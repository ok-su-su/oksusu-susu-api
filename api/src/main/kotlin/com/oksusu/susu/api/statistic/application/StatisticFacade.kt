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
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class StatisticFacade(
    private val categoryService: CategoryService,
    private val userEnvelopeStatisticService: UserEnvelopeStatisticService,
    private val susuEnvelopeStatisticService: SusuEnvelopeStatisticService,
    private val susuSpecificEnvelopeStatisticService: SusuSpecificEnvelopeStatisticService,
    private val relationshipService: RelationshipService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun getUserEnvelopeStatistic(user: AuthUser): UserEnvelopeStatisticResponse {
        return userEnvelopeStatisticService.getStatisticOrNull(user.uid)?.let {
            UserEnvelopeStatisticResponse.from(it)
        } ?: createUserEnvelopeStatistic(user.uid).let {
            UserEnvelopeStatisticResponse.from(it)
        }
    }

    suspend fun refreshUserEnvelopeStatistic(user: AuthUser): UserEnvelopeStatisticResponse {
        val userEnvelopeStatistic = createUserEnvelopeStatistic(user.uid)

        return UserEnvelopeStatisticResponse.from(userEnvelopeStatistic)
    }

    private suspend fun createUserEnvelopeStatistic(uid: Long): UserEnvelopeStatistic {
        val userEnvelopeStatistic = userEnvelopeStatisticService.createUserEnvelopeStatistic(uid)

        /** 유저 봉투 통계 캐싱 */
        eventPublisher.publishEvent(
            CacheUserEnvelopeStatisticEvent(
                uid = uid,
                statistic = userEnvelopeStatistic
            )
        )

        return userEnvelopeStatistic
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
