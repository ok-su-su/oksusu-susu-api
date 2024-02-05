package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.SusuEnvelopeStatisticResponse
import com.oksusu.susu.statistic.model.response.UserEnvelopeStatisticResponse
import com.oksusu.susu.statistic.model.vo.SusuEnvelopeStatisticRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class StatisticFacade(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
    private val susuBasicEnvelopeStatisticService: SusuBasicEnvelopeStatisticService,
    private val susuSpecificEnvelopeStatisticService: SusuSpecificEnvelopeStatisticService,
    private val relationshipService: RelationshipService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getUserEnvelopeStatistic(user: AuthUser): UserEnvelopeStatisticResponse {
        // caching 된거 확인
        userEnvelopeStatisticService.getStatisticOrNull(user.uid)?.run {
            logger.debug { "${user.uid} user statistic cache hit" }
            return UserEnvelopeStatisticResponse.from(this)
        }

        val userEnvelopeStatisticResponse = parZip(
            // 최근 사용 금액
            // 경조사비를 가장 많이 쓴 달
            { envelopeService.countPerHandedOverAtInLast8MonthByUid(user.uid, EnvelopeType.SENT) },
            // 최다 수수 관계
            { friendRelationshipService.countPerRelationshipIdByUid(user.uid) },
            // 최다 수수 경조사
            { envelopeService.countPerCategoryIdByUid(user.uid) },
            { ledgerService.countPerCategoryIdByUid(user.uid) },
            // 가장 많이 받은 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.uid, EnvelopeType.RECEIVED) },
            // 가장 많이 보낸 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.uid, EnvelopeType.SENT) }
        ) {
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                receivedMaxAmount,
                sentMaxAmount,
            ->

            // 최근 사용 금액 + 경조사비를 가장 많이 쓴 달 + 최다 수수 관계 + 최다 수수 경조사
            val basicStatistic = susuBasicEnvelopeStatisticService.constructBasicStatistic(
                envelopHandOverAtMonthCount = envelopHandOverAtMonthCount,
                relationShipConuts = relationShipConuts,
                envelopeCategoryCounts = envelopeCategoryCounts,
                ledgerCategoryCounts = ledgerCategoryCounts
            )

            // 가장 많이 받은 금액
            val receivedMaxAmountModel = receivedMaxAmount?.run {
                TitleValueModel(title = this.friend.name, value = this.envelope.amount)
            }

            // 가장 많이 보낸 금액
            val sentMaxAmountModel = sentMaxAmount?.run {
                TitleValueModel(title = this.friend.name, value = this.envelope.amount)
            }

            UserEnvelopeStatisticResponse.of(
                basicStatistic = basicStatistic,
                receivedMaxAmountModel = receivedMaxAmountModel,
                sentMaxAmountModel = sentMaxAmountModel
            )
        }

        UserEnvelopeStatistic.from(userEnvelopeStatisticResponse).run {
            userEnvelopeStatisticService.save(uid = user.uid, userEnvelopeStatistic = this)
        }

        return userEnvelopeStatisticResponse
    }

    suspend fun getSusuEnvelopeStatistic(requestParam: SusuEnvelopeStatisticRequest): SusuEnvelopeStatisticResponse {
        return parZip(
            { susuSpecificEnvelopeStatisticService.getSusuSpecificStatistic(requestParam) },
            { susuBasicEnvelopeStatisticService.getStatisticOrThrow() }
        ) { tempSpecific, basic ->
            val specific = tempSpecific.apply {
                this.averageCategory?.apply { title = categoryService.getCategory(requestParam.categoryId).name }
                this.averageRelationship?.apply {
                    title = relationshipService.getRelationship(requestParam.relationshipId).relation
                }
            }
            SusuEnvelopeStatisticResponse.of(specific, basic)
        }
    }
}
