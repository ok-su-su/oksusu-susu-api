package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.event.model.CacheUserEnvelopeStatisticEvent
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.SusuEnvelopeStatisticResponse
import com.oksusu.susu.statistic.model.response.UserEnvelopeStatisticResponse
import com.oksusu.susu.statistic.model.vo.SusuEnvelopeStatisticRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class StatisticFacade(
    private val categoryService: CategoryService,
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
    private val susuBasicEnvelopeStatisticService: SusuBasicEnvelopeStatisticService,
    private val susuSpecificEnvelopeStatisticService: SusuSpecificEnvelopeStatisticService,
    private val relationshipService: RelationshipService,
    private val envelopeStatisticService: EnvelopeStatisticService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getUserEnvelopeStatistic(user: AuthUser): UserEnvelopeStatisticResponse {
        /** 통계 캐싱 여부 확인 */
        userEnvelopeStatisticService.getStatisticOrNull(user.uid)?.run {
            logger.debug { "${user.uid} user statistic cache hit" }

            return UserEnvelopeStatisticResponse.from(this)
        }

        val userEnvelopeStatistic = parZip(
            // 최근 사용 금액
            { envelopeStatisticService.getRecentSpent(user.uid) },
            // 최다 수수 관계
            { envelopeStatisticService.getMostFrequentRelationship(user.uid) },
            // 최다 수수 경조사
            { envelopeStatisticService.getMostFrequentCategory(user.uid) },
            // 가장 많이 받은 금액
            { envelopeStatisticService.getMaxReceivedEnvelope(user.uid) },
            // 가장 많이 보낸 금액
            { envelopeStatisticService.getMaxSentEnvelope(user.uid) }
        ) {
                recentSpent,
                mostFrequentRelationShip,
                mostFrequentCategory,
                maxReceivedEnvelope,
                maxSentEnvelope,
            ->

            // 경조사비 가장 많이 쓴 달
            val mostSpentMonth = recentSpent?.maxBy { model -> model.value }?.value

            UserEnvelopeStatistic(
                recentSpent = recentSpent,
                mostSpentMonth = mostSpentMonth,
                mostRelationship = mostFrequentRelationShip,
                mostCategory = mostFrequentCategory,
                highestAmountReceived = maxReceivedEnvelope,
                highestAmountSent = maxSentEnvelope,
            )
        }

        /** 유저 봉투 통계 캐싱 */
        eventPublisher.publishEvent(
            CacheUserEnvelopeStatisticEvent(
                uid = user.uid,
                statistic = userEnvelopeStatistic
            )
        )

        return UserEnvelopeStatisticResponse.from(userEnvelopeStatistic)
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
