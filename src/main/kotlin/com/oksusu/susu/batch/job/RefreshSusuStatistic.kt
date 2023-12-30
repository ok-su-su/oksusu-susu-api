package com.oksusu.susu.batch.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.application.SusuBasicStatisticService
import com.oksusu.susu.statistic.application.SusuSpecificStatisticService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled

class RefreshSusuStatistic(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val susuBasicStatisticService: SusuBasicStatisticService,
    private val susuSpecificStatisticService: SusuSpecificStatisticService,
) {
    val logger = mu.KotlinLogging.logger { }

    @Scheduled(
        cron = "0 0 * * * *",
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-susu-statistic.initial-delay:100}"
    )
    fun refreshSusuStatistic() {
        logger.info { "start susu statistic refresh" }

        CoroutineScope(Dispatchers.IO).launch {
            parZip(
                // 최근 사용 금액
                // 경조사비를 가장 많이 쓴 달
                { envelopeService.countPerHandedOverAtInLast8Month(EnvelopeType.SENT) },
                // 최다 수수 관계
                { friendRelationshipService.countPerRelationshipId() },
                // 최다 수수 경조사
                { envelopeService.countPerCategoryId() },
                { ledgerService.countPerCategoryId() },
                // 평균 수수
                { envelopeService.countAvgAmountPerCategoryIdAndRelationshipIdAndBirth() }
            ) {
                    envelopHandOverAtMonthCount,
                    relationShipConuts,
                    envelopeCategoryCounts,
                    ledgerCategoryCounts,
                    avgAmountModels,
                ->

                // 최근 사용 금액 + 경조사비를 가장 많이 쓴 달 + 최다 수수 관계 + 최다 수수 경조사 레디스 저장
                susuBasicStatisticService.save(
                    susuBasicStatisticService.constructBasicStatistic(
                        envelopHandOverAtMonthCount,
                        relationShipConuts,
                        envelopeCategoryCounts,
                        ledgerCategoryCounts
                    )
                )

                // 평균 수수 레디스 저장
                // key: age_categoryId_relationshipId, value: avg
                avgAmountModels.map { avgAmountModel ->
                    async { susuSpecificStatisticService.save(avgAmountModel) }
                }

                // key: category_categoryId, value: avg
                avgAmountModels.groupBy { it.categoryId }.map { modelsMap ->
                    val key = "category_" + modelsMap.key.toString()
                    val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount }
                    async { susuSpecificStatisticService.save(key, avgAmount) }
                }

                // key: relationship_relationshipId, value: avg
                avgAmountModels.groupBy { it.relationshipId }.map { modelsMap ->
                    val key = "relationship_" + modelsMap.key.toString()
                    val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount }
                    async { susuSpecificStatisticService.save(key, avgAmount) }
                }
            }

            logger.info { "end susu statistic refresh" }
        }
    }
}
