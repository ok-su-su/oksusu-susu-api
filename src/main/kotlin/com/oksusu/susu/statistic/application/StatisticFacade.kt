package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.UserStatisticResponse
import org.springframework.stereotype.Service

@Service
class StatisticFacade(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val userStatisticService: UserStatisticService,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getUserStatistic(user: AuthUser): UserStatisticResponse {
        userStatisticService.findByIdOrNull(user.id)?.run {
            logger.debug { "${user.id} user statistic cache hit" }
            return UserStatisticResponse.from(this)
        }

        val userStatisticResponse = parZip(
            // 최근 사용 금액
            // 경조사비를 가장 많이 쓴 달
            { envelopeService.countPerHandedOverAtInLast8Month(user.id, EnvelopeType.SENT) },
            // 최다 수수 관계
            { friendRelationshipService.countPerRelationshipId(user.id) },
            // 최다 수수 경조사
            { envelopeService.countPerCategoryId(user.id) },
            { ledgerService.countPerCategoryId(user.id) },
            // 가장 많이 받은 금액
            { envelopeService.getMaxAmountEnvelopeInfo(user.id, EnvelopeType.RECEIVED) },
            // 가장 많이 보낸 금액
            { envelopeService.getMaxAmountEnvelopeInfo(user.id, EnvelopeType.SENT) }
        ) {
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                receivedMaxAmount,
                sentMaxAmount,
            ->

            // 최근 사용 금액
            val envelopHandOverAtMonthCountModel = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
                ?.map { count -> TitleValueModel(count.handedOverAtMonth.toString(), count.totalCounts) }

            // 경조사비를 가장 많이 쓴 달
            val mostSpentMonth = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
                ?.maxBy { it.totalCounts }
                ?.handedOverAtMonth!!.toLong()

            // 최다 수수 관계
            val relationShipIdConutModel = relationShipConuts.takeIf { it.isNotEmpty() }
                ?.maxBy { it.totalCounts }
                ?.run {
                    TitleValueModel(
                        title = this.relationship.relation,
                        value = this.totalCounts
                    )
                }

            // 최다 수수 경조사
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
            val categotyMaxCountModel = categoryCounts.takeIf { it.isNotEmpty() }
                ?.maxBy { it.totalCounts }
                ?.let {
                    val category = categoryService.getCategory(it.categoryId)
                    TitleValueModel(title = category.name, value = it.totalCounts)
                }

            // 가장 많이 받은 금액
            val receivedMaxAmountModel =
                receivedMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            // 가장 많이 보낸 금액
            val sentMaxAmountModel =
                sentMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            UserStatisticResponse(
                recentSpent = envelopHandOverAtMonthCountModel,
                mostSpentMonth = mostSpentMonth,
                relationship = relationShipIdConutModel,
                category = categotyMaxCountModel,
                received = receivedMaxAmountModel,
                sent = sentMaxAmountModel
            )
        }

        UserStatistic.from(user.id, userStatisticResponse).run { userStatisticService.save(this) }

        return userStatisticResponse
    }
}
