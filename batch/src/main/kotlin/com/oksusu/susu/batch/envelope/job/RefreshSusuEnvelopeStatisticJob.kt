package com.oksusu.susu.batch.envelope.job

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.key.CacheKeyGenerateHelper
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.cache.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.cache.statistic.infrastructure.SusuEnvelopeStatisticRepository
import com.oksusu.susu.cache.statistic.infrastructure.SusuSpecificEnvelopeStatisticRepository
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.extension.*
import com.oksusu.susu.common.model.TitleValueModel
import com.oksusu.susu.domain.category.domain.Category
import com.oksusu.susu.domain.category.infrastructure.CategoryRepository
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.domain.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.domain.envelope.infrastructure.model.CountPerHandedOverAtModel
import com.oksusu.susu.domain.friend.domain.Relationship
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.RelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.model.CountPerRelationshipIdModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
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
    private val statisticConfig: SusuConfig.StatisticConfig,
    private val adminUserConfig: SusuConfig.AdminUserConfig,
    private val cacheService: CacheService,
) {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val REFRESH_BEFORE_HOURS = 1L

        /** 월별 사용 총 금액 캐시 키 (1년) */
        private const val MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX =
            "monthly-spent-envelop-amount-for-last-year:"

        /** 관계별 총합 캐시 키 */
        private const val RELATIONSHIP_COUNT_PREFIX = "relationship_count:"

        /** 경조사별 총 횟수 캐싱 */
        private const val CATEGORY_COUNT_PREFIX = "category_count:"

        /** 나이, 카테고리, 관계별 금액 총 합 및 개수*/
        private const val SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX = "susu_specific_envelope_statistic_amount:"
        private const val SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX = "susu_specific_envelope_statistic_count:"
    }

    suspend fun refreshSusuEnvelopeStatisticAmount(): Map<String, Long> {
        logger.info { "start refresh susu envelope statistic amount" }

        val (minAmount, maxAmount) = getMaxAndMinAmount()

        val from = LocalDateTime.now().minusMonths(12)
        val to = LocalDateTime.now()
        return parZipWithMDC(
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerHandedOverAtBetweenExceptUid(
                        type = EnvelopeType.SENT,
                        from = from,
                        to = to,
                        minAmount = minAmount,
                        maxAmount = maxAmount,
                        uid = adminUserConfig.adminUserUid
                    )
                }
            },
            { withContext(Dispatchers.IO) { friendRelationshipRepository.countPerRelationshipIdExceptUid(adminUserConfig.adminUserUid) } },
            { withContext(Dispatchers.IO) { envelopeRepository.countPerCategoryIdExceptUid(adminUserConfig.adminUserUid) } },
            { withContext(Dispatchers.IO) { ledgerRepository.countPerCategoryIdExceptUid(adminUserConfig.adminUserUid) } },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerStatisticGroupExceptUid(
                        minAmount,
                        maxAmount,
                        adminUserConfig.adminUserUid
                    )
                }
            }
        ) {
                monthlySpentEnvelopAmountForLastYear,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                totalAmountModels,
            ->
            val cache = mutableMapOf<String, Long>()

            /** 월별 사용 총 금액 캐싱 */
            monthlySpentEnvelopAmountForLastYear.map { count ->
                val key = "$MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX${count.handedOverAtMonth}"
                cache[key] = count.totalAmounts
            }

            /** 나이, 카테고리, 관계별 금액 총 합 key: age:categoryId:relationshipId, value: avg */
            parseIntoEnvelopeSpecificStatisticGroup(totalAmountModels).map { model ->
                val amountKey = "$SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX${model.key}"
                cache[amountKey] = model.value.first
                val countKey = "$SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX${model.key}"
                cache[countKey] = model.value.second
            }

            /** 관계별 총 횟수 캐싱 */
            relationShipConuts.map { relationship ->
                val key = "$RELATIONSHIP_COUNT_PREFIX${relationship.relationshipId}"
                cache[key] = relationship.totalCounts
            }

            /** 경조사별 총 횟수 캐싱 */
            envelopeCategoryCounts.map { category ->
                val key = "$CATEGORY_COUNT_PREFIX${category.categoryId}"
                val currentCount = cache[key] ?: 0
                cache[key] = currentCount + category.totalCounts
            }
            ledgerCategoryCounts.map { category ->
                val key = "$CATEGORY_COUNT_PREFIX${category.categoryId}"
                val currentCount = cache[key] ?: 0
                cache[key] = currentCount + category.totalCounts
            }

            withMDCContext(Dispatchers.IO) { cacheService.set(Cache.getSusuEnvelopeStatisticAmountCache(), cache) }

            logger.info { "finish refresh susu envelope statistic amount" }

            cache
        }
    }

    suspend fun refreshSusuEnvelopeStatistic() {
        logger.info { "start refresh susu statistic" }

        val (minAmount, maxAmount) = getMaxAndMinAmount()

        val targetDate = LocalDateTime.now().minusHours(REFRESH_BEFORE_HOURS)

        val to = LocalDateTime.now()

        val cachedAmount = withContext(Dispatchers.IO) {
            cacheService.getOrNull(Cache.getSusuEnvelopeStatisticAmountCache())
        } ?: refreshSusuEnvelopeStatisticAmount()

        parZipWithMDC(
            { withContext(Dispatchers.IO) { envelopeRepository.getUserCountHadEnvelope() } },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerHandedOverAtBetweenExceptUid(
                        type = EnvelopeType.SENT,
                        from = targetDate,
                        to = to,
                        minAmount = minAmount,
                        maxAmount = maxAmount,
                        uid = adminUserConfig.adminUserUid
                    )
                }
            },
            {
                withContext(Dispatchers.IO) {
                    friendRelationshipRepository.countPerRelationshipIdExceptUidByCreatedAtAfter(
                        uid = adminUserConfig.adminUserUid,
                        targetDate = targetDate
                    )
                }
            },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.countPerCategoryIdExceptUidByCreatedAtAfter(
                        uid = adminUserConfig.adminUserUid,
                        targetDate = targetDate
                    )
                }
            },
            {
                withContext(Dispatchers.IO) {
                    ledgerRepository.countPerCategoryIdExceptUidByCreatedAtAfter(
                        uid = adminUserConfig.adminUserUid,
                        targetDate = targetDate
                    )
                }
            },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getCuttingTotalAmountPerStatisticGroupExceptUidByCreatedAtAfter(
                        min = minAmount,
                        max = maxAmount,
                        uid = adminUserConfig.adminUserUid,
                        targetDate = targetDate
                    )
                }
            },
            { withContext(Dispatchers.IO) { relationshipRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { categoryRepository.findAllByIsActive(true) } }
        ) {
                totalUserCount,
                monthlySpentEnvelopAmountForLastYear,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                /** 통계 그룹 당 총합 수수 (절사) */
                totalAmountModels,
                relationships,
                categories,
            ->
            val cache = mutableMapOf<String, Long>()
            val userCount = totalUserCount - adminUserConfig.adminUserUid.size
            val relationshipMap = relationships.associateBy { relationship -> relationship.id }
            val categoryMap = categories.associateBy { category -> category.id }

            /** 캐시 값 분류 */
            val monthlySpentCache = cachedAmount
                ?.classifyKeyByPrefix(MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX) ?: emptyMap()
            val relationshipCache = cachedAmount
                ?.classifyKeyByPrefix(RELATIONSHIP_COUNT_PREFIX) ?: emptyMap()
            val categoryCache = cachedAmount
                ?.classifyKeyByPrefix(CATEGORY_COUNT_PREFIX) ?: emptyMap()
            val specificAmountCache = cachedAmount
                ?.classifyKeyByPrefix(SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX) ?: emptyMap()
            val specificCountCache = cachedAmount
                ?.classifyKeyByPrefix(SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX) ?: emptyMap()

            /** 최근 사용 금액 1년 */
            val monthlySpent = getMonthlySpentForLastYear(
                monthlySpentEnvelopAmountForLastYear = monthlySpentEnvelopAmountForLastYear,
                monthlySpentCache = monthlySpentCache,
                cache = cache
            )

            /** 최근 사용 금액 8달 */
            val monthlySpentForLast8Months = getMonthlySpentForLast8Months(
                monthlySpent = monthlySpent,
                userCount = userCount
            )

            /** 경조사비 가장 많이 쓴 달 */
            val mostSpentMonth = monthlySpent?.maxBy { model -> model.value }?.title?.substring(4)?.toLong()

            /** 최다 수수 관계 */
            val mostFrequentRelationShip = getMostFrequentRelationShip(
                relationShipConuts = relationShipConuts,
                relationships = relationshipMap,
                relationshipCache = relationshipCache,
                cache = cache
            )

            /** 최다 수수 경조사 */
            val mostFrequentCategory = getMostFrequentCategory(
                envelopeCategoryCounts = envelopeCategoryCounts,
                ledgerCategoryCounts = ledgerCategoryCounts,
                categories = categoryMap,
                categoryCache = categoryCache,
                cache = cache
            )

            /** 최다 수수 경조사 평균 */
            val avgMostFrequentCategory = mostFrequentCategory?.apply { value /= userCount }

            /** 최다 수수 관계 평균 */
            val avgMostFrequentRelationship = mostFrequentRelationShip?.apply { value /= userCount }

            susuEnvelopeStatisticRepository.save(
                SusuEnvelopeStatistic(
                    recentSpent = monthlySpentForLast8Months,
                    mostSpentMonth = mostSpentMonth,
                    mostFrequentCategory = avgMostFrequentCategory,
                    mostFrequentRelationShip = avgMostFrequentRelationship
                )
            )

            /**  평균 수수 레디스 저장 key: age:categoryId:relationshipId, value: avg */
            val specificCache = mutableMapOf<String, Pair<Long, Long>>()

            for ((key, value) in specificAmountCache) {
                specificCache[key] = value to specificCountCache[key]!!
            }

            val sortTypeAmountsMap = parseIntoEnvelopeSpecificStatisticGroup(totalAmountModels).mergePair(specificCache)

            sortTypeAmountsMap.map { map ->
                async {
                    susuSpecificEnvelopeStatisticRepository.save(
                        CacheKeyGenerateHelper.getSusuSpecificStatisticKey(map.key),
                        map.value.first / map.value.second
                    )
                }
            }

            val sortTypeAmounts = sortTypeAmountsMap
                .map { model ->
                    val amountKey = "$SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX${model.key}"
                    cache[amountKey] = model.value.first
                    val countKey = "$SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX${model.key}"
                    cache[countKey] = model.value.second

                    val keys = model.key.split(":")
                    CountAvgAmountPerStatisticGroupModel(
                        birth = keys[0].toLong(),
                        categoryId = keys[1].toLong(),
                        relationshipId = keys[2].toLong(),
                        totalAmounts = model.value.first,
                        counts = model.value.second
                    )
                }

            /** key: susu_category_statistic:categoryId, value: avg */
            sortTypeAmounts.groupBy { it.categoryId }.map { modelsMap ->
                val key = CacheKeyGenerateHelper.getSusuCategoryStatisticKey(modelsMap.key)

                val totalAmounts = modelsMap.value.sumOf { value -> value.totalAmounts }
                val totalCounts = modelsMap.value.sumOf { value -> value.counts }
                val avgAmount = totalAmounts / totalCounts

                async { susuSpecificEnvelopeStatisticRepository.save(key, avgAmount) }
            }

            /** key: susu_relationship_statistic:relationshipId, value: avg */
            sortTypeAmounts.groupBy { it.relationshipId }.map { modelsMap ->
                val key = CacheKeyGenerateHelper.getSusuRelationshipStatisticKey(modelsMap.key)

                val totalAmounts = modelsMap.value.sumOf { value -> value.totalAmounts }
                val totalCounts = modelsMap.value.sumOf { value -> value.counts }
                val avgAmount = totalAmounts / totalCounts

                async { susuSpecificEnvelopeStatisticRepository.save(key, avgAmount) }
            }

            /** amount 값 캐싱 */
            cacheService.set(Cache.getSusuEnvelopeStatisticAmountCache(), cache)

            logger.info { "finish refresh susu statistic" }
        }
    }

    private suspend fun getMaxAndMinAmount(): Pair<Long, Long> {
        val susuEnvelopeConfig = statisticConfig.susuEnvelopeConfig

        val count = withMDCContext(Dispatchers.IO) { envelopeRepository.countExceptUid(adminUserConfig.adminUserUid) }

        val minIdx = (count * susuEnvelopeConfig.minCuttingAverage).roundToLong()
        val maxIdx = (count * susuEnvelopeConfig.maxCuttingAverage).roundToLong()

        return parZipWithMDC(
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getEnvelopeAmountByPositionOrderByAmountExceptUid(
                        minIdx,
                        adminUserConfig.adminUserUid
                    )
                }
            },
            {
                withContext(Dispatchers.IO) {
                    envelopeRepository.getEnvelopeAmountByPositionOrderByAmountExceptUid(
                        maxIdx,
                        adminUserConfig.adminUserUid
                    )
                }
            }
        ) { min, max -> min to max }
    }

    private fun getMonthlySpentForLastYear(
        monthlySpentEnvelopAmountForLastYear: List<CountPerHandedOverAtModel>,
        monthlySpentCache: Map<String, Long>,
        cache: MutableMap<String, Long>,
    ): List<TitleValueModel<Long>>? {
        return monthlySpentEnvelopAmountForLastYear
            .associate { spent -> spent.handedOverAtMonth.toString() to spent.totalAmounts }
            .merge(monthlySpentCache)
            .takeIf { it.isNotEmpty() }
            ?.map { map ->
                val key = "$MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX${map.key}"
                cache[key] = map.value
                TitleValueModel(map.key, map.value)
            }
            ?.sortedBy { model -> model.title }
    }

    private fun getMonthlySpentForLast8Months(
        monthlySpent: List<TitleValueModel<Long>>?,
        userCount: Long,
    ): List<TitleValueModel<Long>>? {
        val before8Month = LocalDate.now().minusMonths(7).yearMonth()

        return monthlySpent?.filter { spent -> spent.title >= before8Month }
            ?.map { model -> model.apply { value /= userCount } }
    }

    private fun getMostFrequentRelationShip(
        relationShipConuts: List<CountPerRelationshipIdModel>,
        relationships: Map<Long, Relationship>,
        relationshipCache: Map<String, Long>,
        cache: MutableMap<String, Long>,
    ): TitleValueModel<Long>? {
        return relationShipConuts.associate { relationship -> relationship.relationshipId.toString() to relationship.totalCounts }
            .merge(relationshipCache)
            .map { map ->
                val key = "$RELATIONSHIP_COUNT_PREFIX${map.key}"
                cache[key] = map.value
                map
            }
            .maxByOrNull { map -> map.value }
            ?.run {
                TitleValueModel(
                    title = relationships[this.key.toLong()]!!.relation,
                    value = this.value
                )
            }
    }

    private fun getMostFrequentCategory(
        envelopeCategoryCounts: List<CountPerCategoryIdModel>,
        ledgerCategoryCounts: List<CountPerCategoryIdModel>,
        categories: Map<Long, Category>,
        categoryCache: Map<String, Long>,
        cache: MutableMap<String, Long>,
    ): TitleValueModel<Long>? {
        val countMap = mutableMapOf<String, Long>()

        envelopeCategoryCounts.map { category ->
            val currentCount = countMap[category.categoryId.toString()] ?: 0
            countMap[category.categoryId.toString()] = currentCount + category.totalCounts
        }
        ledgerCategoryCounts.map { category ->
            val currentCount = countMap[category.categoryId.toString()] ?: 0
            countMap[category.categoryId.toString()] = currentCount + category.totalCounts
        }

        return countMap.merge(categoryCache)
            .map { map ->
                val key = "$CATEGORY_COUNT_PREFIX${map.key}"
                cache[key] = map.value
                map
            }
            .maxByOrNull { it.value }
            ?.run {
                val category = categories[this.key.toLong()]
                TitleValueModel(title = category!!.name, value = this.value)
            }
    }

    private fun parseIntoEnvelopeSpecificStatisticGroup(totalAmountModels: List<CountAvgAmountPerStatisticGroupModel>): Map<String, Pair<Long, Long>> {
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
            group.key to (totalAmounts to totalCounts)
        }.toMap()
    }
}
