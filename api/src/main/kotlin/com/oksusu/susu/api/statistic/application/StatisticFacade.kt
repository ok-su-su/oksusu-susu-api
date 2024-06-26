package com.oksusu.susu.api.statistic.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.event.model.CacheUserEnvelopeStatisticEvent
import com.oksusu.susu.api.friend.application.RelationshipService
import com.oksusu.susu.api.statistic.model.response.SusuEnvelopeStatisticResponse
import com.oksusu.susu.api.statistic.model.response.UserEnvelopeStatisticResponse
import com.oksusu.susu.api.statistic.model.vo.SusuEnvelopeStatisticRequest
import com.oksusu.susu.cache.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.common.extension.yearMonth
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StatisticFacade(
    private val categoryService: CategoryService,
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
    private val susuEnvelopeStatisticService: SusuEnvelopeStatisticService,
    private val susuSpecificEnvelopeStatisticService: SusuSpecificEnvelopeStatisticService,
    private val relationshipService: RelationshipService,
    private val envelopeStatisticService: EnvelopeStatisticService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun getUserEnvelopeStatistic(user: AuthUser): UserEnvelopeStatisticResponse {
        /** 통계 캐싱 여부 확인 */
        userEnvelopeStatisticService.getStatisticOrNull(user.uid)?.run {
            logger.debug { "${user.uid} user statistic cache hit" }

            return UserEnvelopeStatisticResponse.from(this)
        }

        val userEnvelopeStatistic = parZipWithMDC(
            /** 최근 사용 금액 1년 */
            { envelopeStatisticService.getRecentSpentFor1Year(user.uid) },
            /** 최다 수수 관계 */
            { envelopeStatisticService.getMostFrequentRelationship(user.uid) },
            /** 최다 수수 경조사 */
            { envelopeStatisticService.getMostFrequentCategory(user.uid) },
            /** 가장 많이 받은 금액 */
            { envelopeStatisticService.getMaxReceivedEnvelope(user.uid) },
            /** 가장 많이 보낸 금액 */
            { envelopeStatisticService.getMaxSentEnvelope(user.uid) }
        ) {
                recentSpent,
                mostFrequentRelationShip,
                mostFrequentCategory,
                maxReceivedEnvelope,
                maxSentEnvelope,
            ->

            /** 최근 사용 금액 8달 */
            val before8Month = LocalDate.now().minusMonths(7).yearMonth()
            val recentSpentForLast8Months = recentSpent?.filter { spent ->
                spent.title >= before8Month
            }

            /** 경조사비 가장 많이 쓴 달 */
            val mostSpentMonth = recentSpent?.maxBy { model -> model.value }?.title?.substring(4)?.toLong()

            UserEnvelopeStatistic(
                recentSpent = recentSpentForLast8Months,
                mostSpentMonth = mostSpentMonth,
                mostFrequentRelationShip = mostFrequentRelationShip,
                mostFrequentCategory = mostFrequentCategory,
                maxReceivedEnvelope = maxReceivedEnvelope,
                maxSentEnvelope = maxSentEnvelope
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
        return parZipWithMDC(
            { susuSpecificEnvelopeStatisticService.getStatistic(requestParam) },
            { susuEnvelopeStatisticService.getStatisticOrThrow() }
        ) { tempSpecific, statistic ->
            val specific = tempSpecific.apply {
                this.averageCategory?.apply { title = categoryService.getCategory(requestParam.categoryId).name }
                this.averageRelationship?.apply {
                    title = relationshipService.getRelationship(requestParam.relationshipId).relation
                }
            }

            SusuEnvelopeStatisticResponse.of(specific, statistic)
        }
    }
}
