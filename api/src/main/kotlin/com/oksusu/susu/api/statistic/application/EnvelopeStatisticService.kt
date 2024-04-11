package com.oksusu.susu.api.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.envelope.application.EnvelopeService
import com.oksusu.susu.api.envelope.application.LedgerService
import com.oksusu.susu.api.friend.application.FriendRelationshipService
import com.oksusu.susu.api.friend.application.RelationshipService
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.model.TitleValueModel
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.model.CountPerCategoryIdModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.roundToLong

@Service
class EnvelopeStatisticService(
    private val envelopeService: EnvelopeService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val friendRelationshipService: FriendRelationshipService,
    private val relationshipService: RelationshipService,
    private val statisticConfig: SusuConfig.StatisticConfig,
) {
    val logger = KotlinLogging.logger {}

    /** 가장 많이 보낸 금액 */
    suspend fun getMaxSentEnvelope(uid: Long): TitleValueModel<Long>? {
        val sentMaxAmount = envelopeService.getMaxAmountEnvelopeInfoByUid(uid, EnvelopeType.SENT)

        return sentMaxAmount?.run {
            TitleValueModel(title = this.friend.name, value = this.envelope.amount)
        }
    }

    /** 가장 많이 받은 금액 */
    suspend fun getMaxReceivedEnvelope(uid: Long): TitleValueModel<Long>? {
        val receivedMaxAmount = envelopeService.getMaxAmountEnvelopeInfoByUid(uid, EnvelopeType.RECEIVED)

        return receivedMaxAmount?.run {
            TitleValueModel(title = this.friend.name, value = this.envelope.amount)
        }
    }

    /** 최다 수수 경조사 */
    suspend fun getMostFrequentCategory(uid: Long?): TitleValueModel<Long>? {
        val (envelopeCategoryCounts, ledgerCategoryCounts) = if (uid == null) {
            parZip(
                { envelopeService.countPerCategoryId() },
                { ledgerService.countPerCategoryId() }
            ) { envelopeCategoryCounts, ledgerCategoryCounts -> envelopeCategoryCounts to ledgerCategoryCounts }
        } else {
            parZip(
                { envelopeService.countPerCategoryIdByUid(uid) },
                { ledgerService.countPerCategoryIdByUid(uid) }
            ) { envelopeCategoryCounts, ledgerCategoryCounts -> envelopeCategoryCounts to ledgerCategoryCounts }
        }

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
                val category = categoryService.getCategory(it.categoryId)
                TitleValueModel(title = category.name, value = it.totalCounts)
            }
    }

    /** 최다 수수 관계 */
    suspend fun getMostFrequentRelationship(uid: Long?): TitleValueModel<Long>? {
        val relationShipConuts = if (uid == null) {
            friendRelationshipService.countPerRelationshipId()
        } else {
            friendRelationshipService.countPerRelationshipIdByUid(uid)
        }

        return relationShipConuts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.run {
                TitleValueModel(
                    title = relationshipService.getRelationship(this.relationshipId).relation,
                    value = this.totalCounts
                )
            }
    }

    /** 최근 사용 금액 */
    suspend fun getRecentSpentFor1Year(uid: Long): List<TitleValueModel<Long>>? {
        val envelopHandOverAtMonthCount =
            envelopeService.getTotalAmountPerHandedOverAtInLast1YearByUid(uid, EnvelopeType.SENT)

        return envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.map { count ->
                TitleValueModel(count.handedOverAtMonth.toString(), count.totalAmounts)
            }?.sortedBy { model -> model.title }
    }

    /** 최근 사용 금액 (절사) */
    suspend fun getCuttingRecentSpentFor1Year(minAmount: Long, maxAmount: Long): List<TitleValueModel<Long>>? {
        val envelopHandOverAtMonthCount =
            envelopeService.getCuttingTotalAmountPerHandedOverAtInLast1Year(EnvelopeType.SENT, minAmount, maxAmount)

        return envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.map { count ->
                TitleValueModel(count.handedOverAtMonth.toString(), count.totalAmounts)
            }?.sortedBy { model -> model.title }
    }

    /** 절사 평균 limit 지점 amount 구하기 */
    suspend fun getLimitAmountForCutting(): Pair<Long, Long> {
        val susuEnvelopeConfig = statisticConfig.susuEnvelopeConfig

        val count = envelopeService.count()

        val minIdx = (count * susuEnvelopeConfig.minCuttingAverage).roundToLong()
        val maxIdx = (count * susuEnvelopeConfig.maxCuttingAverage).roundToLong()

        return parZip(
            { envelopeService.getEnvelopeByPositionOrderByAmount(minIdx) },
            { envelopeService.getEnvelopeByPositionOrderByAmount(maxIdx) }
        ) { min, max -> min.amount to max.amount }
    }
}
