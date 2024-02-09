package com.oksusu.susu.batch.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.extension.toStatisticAgeGroup
import com.oksusu.susu.extension.yearMonth
import com.oksusu.susu.statistic.application.EnvelopeStatisticService
import com.oksusu.susu.statistic.application.SusuEnvelopeStatisticService
import com.oksusu.susu.statistic.application.SusuSpecificEnvelopeStatisticService
import com.oksusu.susu.statistic.domain.SusuEnvelopeStatistic
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class RefreshSusuEnvelopeStatisticJob(
    private val envelopeService: EnvelopeService,
    private val susuEnvelopeStatisticService: SusuEnvelopeStatisticService,
    private val susuSpecificEnvelopeStatisticService: SusuSpecificEnvelopeStatisticService,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
    private val envelopeStatisticService: EnvelopeStatisticService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun refreshSusuEnvelopeStatistic() {
        logger.info { "start refresh susu statistic" }

        parZip(
            /** 봉투 소유 유저 수 */
            { envelopeService.getUserCountHadEnvelope() },
            /** 최근 사용 금액 */
            { envelopeStatisticService.getRecentSpent(null) },
            /** 최다 수수 관계 */
            { envelopeStatisticService.getMostFrequentRelationship(null) },
            /** 최다 수수 경조사 */
            { envelopeStatisticService.getMostFrequentCategory(null) },
            /** 평균 수수 */
            { envelopeService.countAvgAmountPerStatisticGroup() }
        ) {
                userCount,
                recentSpent,
                mostFrequentRelationShip,
                mostFrequentCategory,
                avgAmountModels,
            ->

            /** 최근 사용 금액 8달 */
            val before8Month = LocalDate.now().minusMonths(7).yearMonth()
            val recentSpentForLast8Months = recentSpent?.filter { spent -> spent.title >= before8Month }
                ?.map { model -> model.apply { value /= userCount } }

            /** 경조사비 가장 많이 쓴 달 */
            val mostSpentMonth = recentSpent?.maxBy { model -> model.value }?.title?.substring(4)?.toLong()

            /** 최다 수수 경조사 평균 */
            val avgMostFrequentCategory = mostFrequentCategory?.apply { value /= userCount }

            /** 최다 수수 관계 평균 */
            val avgMostFrequentRelationship = mostFrequentRelationShip?.apply { value /= userCount }

            susuEnvelopeStatisticService.save(
                SusuEnvelopeStatistic(
                    recentSpent = recentSpentForLast8Months,
                    mostSpentMonth = mostSpentMonth,
                    mostFrequentCategory = avgMostFrequentCategory,
                    mostFrequentRelationShip = avgMostFrequentRelationship
                )
            )

            /**  평균 수수 레디스 저장 key: age:categoryId:relationshipId, value: avg */
            parseIntoGroup(avgAmountModels).map { model ->
                async {
                    susuSpecificEnvelopeStatisticService.save(
                        cacheKeyGenerateHelper.getSusuSpecificStatisticKey(model.key),
                        model.value
                    )
                }
            }

            /** key: susu_category_statistic:categoryId, value: avg */
            avgAmountModels.groupBy { it.categoryId }.map { modelsMap ->
                val key = cacheKeyGenerateHelper.getSusuCategoryStatisticKey(modelsMap.key)
                val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount } / modelsMap.value.size

                async { susuSpecificEnvelopeStatisticService.save(key, avgAmount) }
            }

            /** key: susu_relationship_statistic:relationshipId, value: avg */
            avgAmountModels.groupBy { it.relationshipId }.map { modelsMap ->
                val key = cacheKeyGenerateHelper.getSusuRelationshipStatisticKey(modelsMap.key)
                val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount } / modelsMap.value.size

                async { susuSpecificEnvelopeStatisticService.save(key, avgAmount) }
            }

            logger.info { "finish refresh susu statistic" }
        }
    }

    private fun parseIntoGroup(avgAmountModels: List<CountAvgAmountPerStatisticGroupModel>): Map<String, Long> {
        /** key: age, value: list<model> */
        val ages = avgAmountModels.groupBy { it.birth.toStatisticAgeGroup() }

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
            group.key to group.value.sumOf { value -> value.averageAmount } / group.value.size
        }.toMap()
    }
}
