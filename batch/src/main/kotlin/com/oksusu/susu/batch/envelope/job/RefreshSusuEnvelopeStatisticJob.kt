package com.oksusu.susu.batch.envelope.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.toStatisticAgeGroup
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.common.extension.yearMonth
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.domain.category.domain.Category
import com.oksusu.susu.domain.category.infrastructure.CategoryRepository
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.domain.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.domain.friend.domain.Relationship
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.RelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.model.CountPerRelationshipIdModel
import com.oksusu.susu.domain.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.common.model.TitleValueModel
import com.oksusu.susu.domain.statistic.infrastructure.redis.SusuEnvelopeStatisticRepository
import com.oksusu.susu.domain.statistic.infrastructure.redis.SusuSpecificEnvelopeStatisticRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.roundToLong

@Component
class RefreshSusuEnvelopeStatisticJob(
    private val susuEnvelopeStatisticRepository: SusuEnvelopeStatisticRepository,
    private val susuSpecificEnvelopeStatisticRepository: SusuSpecificEnvelopeStatisticRepository,
    private val envelopeRepository: EnvelopeRepository,
    private val friendRelationshipRepository: FriendRelationshipRepository,
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val relationshipRepository: RelationshipRepository,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
    private val statisticConfig: SusuConfig.StatisticConfig,
) {
    val logger = KotlinLogging.logger { }

    suspend fun refreshSusuEnvelopeStatistic() {
        logger.info { "start refresh susu statistic" }

        val (minAmount, maxAmount) = getMaxAndMinAmount()

        val from = LocalDate.now().minusMonths(11).atTime(0, 0)
        val to = LocalDate.now().atTime(23, 59)
        parZip(
            Dispatchers.IO + MDCContext(),
            { withContext(Dispatchers.IO) { envelopeRepository.getUserCountHadEnvelope() } },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerHandedOverAtBetween(
                        type = EnvelopeType.SENT,
                        from = from,
                        to = to,
                        minAmount = minAmount,
                        maxAmount = maxAmount
                    )
                }
            },
            { withContext(Dispatchers.IO) { friendRelationshipRepository.countPerRelationshipId() } },
            { withContext(Dispatchers.IO) { envelopeRepository.countPerCategoryId() } },
            { withContext(Dispatchers.IO) { ledgerRepository.countPerCategoryId() } },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerStatisticGroup(minAmount, maxAmount)
                }
            },
            { withContext(Dispatchers.IO) { relationshipRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { categoryRepository.findAllByIsActive(true) } }
        ) {
                /** 봉투 소유 유저 수 */
                userCount,
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                /** 통계 그룹 당 총합 수수 (절사) */
                totalAmountModels,
                relationships,
                categories,
            ->
            val relationshipMap = relationships.associateBy { relationship -> relationship.id }
            val categoryMap = categories.associateBy { category -> category.id }

            /** 최근 사용 금액 1년 */
            val recentSpent = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
                ?.map { count ->
                    TitleValueModel(count.handedOverAtMonth.toString(), count.totalAmounts)
                }?.sortedBy { model -> model.title }

            /** 최근 사용 금액 8달 */
            val recentSpentForLast8Months = getRecentSpentForLast8Months(
                recentSpent = recentSpent,
                userCount = userCount
            )

            /** 최다 수수 관계 */
            val mostFrequentRelationShip = getMostFrequentRelationShip(
                relationShipConuts = relationShipConuts,
                relationships = relationshipMap
            )

            /** 최다 수수 경조사 */
            val mostFrequentCategory = getMostFrequentCategory(
                envelopeCategoryCounts = envelopeCategoryCounts,
                ledgerCategoryCounts = ledgerCategoryCounts,
                categories = categoryMap
            )

            /** 경조사비 가장 많이 쓴 달 */
            val mostSpentMonth = recentSpent?.maxBy { model -> model.value }?.title?.substring(4)?.toLong()

            /** 최다 수수 경조사 평균 */
            val avgMostFrequentCategory = mostFrequentCategory?.apply { value /= userCount }

            /** 최다 수수 관계 평균 */
            val avgMostFrequentRelationship = mostFrequentRelationShip?.apply { value /= userCount }

            susuEnvelopeStatisticRepository.save(
                SusuEnvelopeStatistic(
                    recentSpent = recentSpentForLast8Months,
                    mostSpentMonth = mostSpentMonth,
                    mostFrequentCategory = avgMostFrequentCategory,
                    mostFrequentRelationShip = avgMostFrequentRelationship
                )
            )

            /**  평균 수수 레디스 저장 key: age:categoryId:relationshipId, value: avg */
            parseIntoGroup(totalAmountModels).map { model ->
                async {
                    susuSpecificEnvelopeStatisticRepository.save(
                        cacheKeyGenerateHelper.getSusuSpecificStatisticKey(model.key),
                        model.value
                    )
                }
            }

            /** key: susu_category_statistic:categoryId, value: avg */
            totalAmountModels.groupBy { it.categoryId }.map { modelsMap ->
                val key = cacheKeyGenerateHelper.getSusuCategoryStatisticKey(modelsMap.key)

                val totalAmounts = modelsMap.value.sumOf { value -> value.totalAmounts }
                val totalCounts = modelsMap.value.sumOf { value -> value.counts }
                val avgAmount = totalAmounts / totalCounts

                async { susuSpecificEnvelopeStatisticRepository.save(key, avgAmount) }
            }

            /** key: susu_relationship_statistic:relationshipId, value: avg */
            totalAmountModels.groupBy { it.relationshipId }.map { modelsMap ->
                val key = cacheKeyGenerateHelper.getSusuRelationshipStatisticKey(modelsMap.key)

                val totalAmounts = modelsMap.value.sumOf { value -> value.totalAmounts }
                val totalCounts = modelsMap.value.sumOf { value -> value.counts }
                val avgAmount = totalAmounts / totalCounts

                async { susuSpecificEnvelopeStatisticRepository.save(key, avgAmount) }
            }

            logger.info { "finish refresh susu statistic" }
        }
    }

    private suspend fun getMaxAndMinAmount(): Pair<Long, Long> {
        val susuEnvelopeConfig = statisticConfig.susuEnvelopeConfig

        val count = withMDCContext(Dispatchers.IO) {
            envelopeRepository.count()
        }

        val minIdx = (count * susuEnvelopeConfig.minCuttingAverage).roundToLong()
        val maxIdx = (count * susuEnvelopeConfig.maxCuttingAverage).roundToLong()

        return parZip(
            { withContext(Dispatchers.IO) { envelopeRepository.getEnvelopeByPositionOrderByAmount(minIdx) } },
            { withContext(Dispatchers.IO) { envelopeRepository.getEnvelopeByPositionOrderByAmount(maxIdx) } }
        ) { min, max ->
            val minAmount = min.takeIf { it.isNotEmpty() }
                ?.first()?.amount
                ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)
            val maxAmount = max.takeIf { it.isNotEmpty() }
                ?.first()?.amount
                ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)

            minAmount to maxAmount
        }
    }

    private fun getRecentSpentForLast8Months(
        recentSpent: List<TitleValueModel<Long>>?,
        userCount: Long,
    ): List<TitleValueModel<Long>>? {
        val before8Month = LocalDate.now().minusMonths(7).yearMonth()
        return recentSpent?.filter { spent -> spent.title >= before8Month }
            ?.map { model -> model.apply { value /= userCount } }
    }

    private fun getMostFrequentRelationShip(
        relationShipConuts: List<CountPerRelationshipIdModel>,
        relationships: Map<Long, Relationship>,
    ): TitleValueModel<Long>? {
        return relationShipConuts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.run {
                TitleValueModel(
                    title = relationships[this.relationshipId]!!.relation,
                    value = this.totalCounts
                )
            }
    }

    private fun getMostFrequentCategory(
        envelopeCategoryCounts: List<CountPerCategoryIdModel>,
        ledgerCategoryCounts: List<CountPerCategoryIdModel>,
        categories: Map<Long, Category>,
    ): TitleValueModel<Long>? {
        val categoryIdSet = envelopeCategoryCounts.map { count -> count.categoryId }.toSet()
            .union(ledgerCategoryCounts.map { count -> count.categoryId })
        val categoryCounts = categoryIdSet.map { id ->
            val envelopeCount = envelopeCategoryCounts.firstOrNull { it.categoryId == id }
                ?.totalCounts ?: 0L
            val ledgerCount = ledgerCategoryCounts.firstOrNull { it.categoryId == id }
                ?.totalCounts ?: 0L
            CountPerCategoryIdModel(
                categoryId = id,
                totalCounts = envelopeCount + ledgerCount
            )
        }

        return categoryCounts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.let {
                val category = categories[it.categoryId]
                TitleValueModel(title = category!!.name, value = it.totalCounts)
            }
    }

    private fun parseIntoGroup(totalAmountModels: List<CountAvgAmountPerStatisticGroupModel>): Map<String, Long> {
        /** key: age, value: list<model> */
        val ages = totalAmountModels.groupBy { it.birth.toStatisticAgeGroup() }

        /** key: age:categoryId, value: list<model> */
        val ageCategorys = ages.flatMap { age ->
            val ageCategories = age.value.groupBy { it.categoryId }

            ageCategories.map { ageCategory ->
                "${age.key}:${ageCategory.key}" to ageCategory.value
            }
        }.associate { ageCategory -> ageCategory.first to ageCategory.second }

        /** key: age:categoryId:relationshipId, value: list<model> */
        val groups = ageCategorys.flatMap { ageCategory ->
            val groups = ageCategory.value.groupBy { it.relationshipId }

            groups.map { group ->
                "${ageCategory.key}:${group.key}" to group.value
            }
        }.associate { group -> group.first to group.second }

        /** key: age:categoryId:relationshipId, value: avg */
        return groups.map { group ->
            val totalAmounts = group.value.sumOf { value -> value.totalAmounts }
            val totalCounts = group.value.sumOf { value -> value.counts }
            group.key to totalAmounts / totalCounts
        }.toMap()
    }
}
