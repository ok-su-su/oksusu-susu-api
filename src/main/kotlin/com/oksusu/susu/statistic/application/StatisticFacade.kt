package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.extension.toJson
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.SusuStatisticResponse
import com.oksusu.susu.statistic.model.response.UserStatisticResponse
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
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
    val logger = mu.KotlinLogging.logger { }

    suspend fun getUserStatistic(user: AuthUser): UserStatisticResponse {
        // caching 된거 확인
        userStatisticService.findByIdOrNull(user.id)?.run {
            logger.debug { "${user.id} user statistic cache hit" }
            return this.userStatisticResponse
        }

        val userStatisticResponse = parZip(
            // 최근 사용 금액
            // 경조사비를 가장 많이 쓴 달
            { envelopeService.countPerHandedOverAtInLast8MonthByUid(user.id, EnvelopeType.SENT) },
            // 최다 수수 관계
            { friendRelationshipService.countPerRelationshipIdByUid(user.id) },
            // 최다 수수 경조사
            { envelopeService.countPerCategoryIdByUid(user.id) },
            { ledgerService.countPerCategoryIdByUid(user.id) },
            // 가장 많이 받은 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.id, EnvelopeType.RECEIVED) },
            // 가장 많이 보낸 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.id, EnvelopeType.SENT) }
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
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts
            )

            // 가장 많이 받은 금액
            val receivedMaxAmountModel =
                receivedMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            // 가장 많이 보낸 금액
            val sentMaxAmountModel =
                sentMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            UserStatisticResponse.of(
                basicStatistic,
                receivedMaxAmountModel,
                sentMaxAmountModel
            )
        }

        UserStatistic.from(user.id, userStatisticResponse).run { userStatisticService.save(this) }

        logger.info { jacksonObjectMapper().readValue(userStatisticResponse.toJson(), UserStatisticResponse::class.java) }
        return userStatisticResponse
    }

    suspend fun getSusuStatistic(requestParam: SusuStatisticRequest): SusuStatisticResponse {
        return parZip(
            { susuSpecificStatisticService.getLatestSusuSpecificStatistic(requestParam) },
            { susuBasicStatisticService.getLatestSusuBasicStatistic() }
        ) { tempSpecific, basic ->
            val specific = tempSpecific.apply {
                this.averageCategory?.apply { title = categoryService.getCategory(requestParam.category).name }
                this.averageRelationship?.apply {
                    title =
                        relationshipService.getRelationship(requestParam.relationship).relation
                }
            }
            SusuStatisticResponse.of(specific, basic.statistic)
        }
    }
}
