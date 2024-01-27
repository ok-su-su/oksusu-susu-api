package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.SusuStatisticResponse
import com.oksusu.susu.statistic.model.response.UserStatisticResponse
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class StatisticFacade(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val userStatisticService: UserStatisticService,
    private val susuBasicStatisticService: SusuBasicStatisticService,
    private val susuSpecificStatisticService: SusuSpecificStatisticService,
    private val relationshipService: RelationshipService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getUserStatistic(user: AuthUser): UserStatisticResponse {
        // caching 된거 확인
        userStatisticService.getStatisticOrNull(user.uid)?.run {
            logger.debug { "${user.uid} user statistic cache hit" }
            return UserStatisticResponse.from(this)
        }

        val userStatisticResponse = parZip(
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
            val basicStatistic = susuBasicStatisticService.constructBasicStatistic(
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

            UserStatisticResponse.of(
                basicStatistic = basicStatistic,
                receivedMaxAmountModel = receivedMaxAmountModel,
                sentMaxAmountModel = sentMaxAmountModel
            )
        }

        UserStatistic.from(userStatisticResponse).run {
            userStatisticService.save(uid = user.uid, userStatistic = this)
        }

        return userStatisticResponse
    }

    suspend fun getSusuStatistic(requestParam: SusuStatisticRequest): SusuStatisticResponse {
        return parZip(
            { susuSpecificStatisticService.getSusuSpecificStatistic(requestParam) },
            { susuBasicStatisticService.getStatisticOrThrow() }
        ) { tempSpecific, basic ->
            val specific = tempSpecific.apply {
                this.averageCategory?.apply { title = categoryService.getCategory(requestParam.postCategoryId).name }
                this.averageRelationship?.apply {
                    title = relationshipService.getRelationship(requestParam.relationshipId).relation
                }
            }
            SusuStatisticResponse.of(specific, basic)
        }
    }
}
