package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.application.LedgerService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.statistic.model.TitleValueModel
import org.springframework.stereotype.Service

@Service
class EnvelopeStatisticService(
    private val envelopeService: EnvelopeService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val friendRelationshipService: FriendRelationshipService,
    private val relationshipService: RelationshipService,
) {
    /** 가장 많이 보낸 금액 */
    suspend fun getMaxReceivedEnvelope(uid: Long): TitleValueModel<Long>? {
        val sentMaxAmount = envelopeService.getMaxAmountEnvelopeInfoByUid(uid, EnvelopeType.SENT)

        return sentMaxAmount?.run {
            TitleValueModel(title = this.friend.name, value = this.envelope.amount)
        }
    }

    /** 가장 많이 받은 금액 */
    suspend fun getMaxSentEnvelope(uid: Long): TitleValueModel<Long>? {
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
    suspend fun getRecentSpent(uid: Long?): List<TitleValueModel<Long>>? {
        val envelopHandOverAtMonthCount = if (uid == null) {
            envelopeService.countPerHandedOverAtInLast8Month(EnvelopeType.SENT)
        } else {
            envelopeService.countPerHandedOverAtInLast8MonthByUid(uid, EnvelopeType.SENT)
        }

        return envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.map { count ->
                TitleValueModel(count.handedOverAtMonth.toString(), count.totalCounts)
            }?.sortedBy { model -> model.title }
    }
}
