package com.oksusu.susu.batch.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.common.consts.SUSU_CATEGORY_STATISTIC_KEY_PREFIX
import com.oksusu.susu.common.consts.SUSU_RELATIONSHIP_STATISTIC_KEY_PREFIX
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.extension.toAgeGroup
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.application.SusuBasicStatisticService
import com.oksusu.susu.statistic.application.SusuSpecificStatisticService
import kotlinx.coroutines.async
import org.springframework.stereotype.Component

@Component
class RefreshSusuStatisticJob(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val susuBasicStatisticService: SusuBasicStatisticService,
    private val susuSpecificStatisticService: SusuSpecificStatisticService,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun refreshSusuStatistic() {
        logger.info { "start susu statistic refresh" }

        parZip(
            /** 최근 사용 금액, 경조사비를 가장 많이 쓴 달 */
            { envelopeService.countPerHandedOverAtInLast8Month(EnvelopeType.SENT) },
            /** 최다 수수 관계 */
            { friendRelationshipService.countPerRelationshipId() },
            /** 최다 수수 경조사 */
            { envelopeService.countPerCategoryId() },
            { ledgerService.countPerCategoryId() },
            /** 평균 수수 */
            { envelopeService.countAvgAmountPerCategoryIdAndRelationshipIdAndBirth() }
        ) { envelopHandOverAtMonthCount,
            relationShipConuts,
            envelopeCategoryCounts,
            ledgerCategoryCounts,
            avgAmountModels, ->

            /** 최근 사용 금액 + 경조사비를 가장 많이 쓴 달 + 최다 수수 관계 + 최다 수수 경조사 레디스 저장 */
            susuBasicStatisticService.save(
                susuBasicStatisticService.constructBasicStatistic(
                    envelopHandOverAtMonthCount,
                    relationShipConuts,
                    envelopeCategoryCounts,
                    ledgerCategoryCounts
                )
            )

            /**  평균 수수 레디스 저장 key: age:categoryId:relationshipId, value: avg */
            parseIntoGroup(avgAmountModels).map { model ->
                async {
                    susuSpecificStatisticService.save(
                        cacheKeyGenerateHelper.getSusuSpecificStatisticKey(model.key),
                        model.value
                    )
                }
            }

            /** key: susu_category_statistic:categoryId, value: avg */
            avgAmountModels.groupBy { it.categoryId }.map { modelsMap ->
                val key = SUSU_CATEGORY_STATISTIC_KEY_PREFIX + modelsMap.key.toString()
                val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount } / modelsMap.value.size
                async { susuSpecificStatisticService.save(key, avgAmount) }
            }

            /** key: susu_relationship_statistic:relationshipId, value: avg */
            avgAmountModels.groupBy { it.relationshipId }.map { modelsMap ->
                val key = SUSU_RELATIONSHIP_STATISTIC_KEY_PREFIX + modelsMap.key.toString()
                val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount } / modelsMap.value.size
                async { susuSpecificStatisticService.save(key, avgAmount) }
            }

            logger.info { "end susu statistic refresh" }
        }
    }

    private fun parseIntoGroup(avgAmountModels: List<CountAvgAmountPerStatisticGroupModel>): Map<String, Long> {
        val ages = avgAmountModels.groupBy { it.birth.toAgeGroup() }

        val ageCategorys = ages.flatMap { age ->
            val ageCategories = age.value.groupBy { it.categoryId }
            ageCategories.map { ageCategory ->
                "${age.key}:${ageCategory.key}" to ageCategory.value
            }
        }.associate { ageCategory -> ageCategory.first to ageCategory.second }

        return ageCategorys.flatMap { ageCategory ->
            val groups = ageCategory.value.groupBy { it.relationshipId }
            groups.map { group ->
                "${ageCategory.key}:${group.key}" to ageCategory.value
            }
        }.associate { group -> group.first to group.second.sumOf { it.averageAmount } / group.second.size }
    }
}
