package com.oksusu.susu.api.statistic.application

import com.oksusu.susu.cache.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.cache.statistic.infrastructure.UserEnvelopeStatisticRepository
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.common.extension.yearMonth
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserEnvelopeStatisticService(
    private val userEnvelopeStatisticRepository: UserEnvelopeStatisticRepository,
    private val envelopeStatisticService: EnvelopeStatisticService,
) {
    suspend fun createUserEnvelopeStatistic(uid: Long): UserEnvelopeStatistic {
        return parZipWithMDC(
            /** 최근 사용 금액 1년 */
            { envelopeStatisticService.getRecentSpentFor1Year(uid) },
            /** 최다 수수 관계 */
            { envelopeStatisticService.getMostFrequentRelationship(uid) },
            /** 최다 수수 경조사 */
            { envelopeStatisticService.getMostFrequentCategory(uid) },
            /** 가장 많이 받은 금액 */
            { envelopeStatisticService.getMaxReceivedEnvelope(uid) },
            /** 가장 많이 보낸 금액 */
            { envelopeStatisticService.getMaxSentEnvelope(uid) }
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
    }

    suspend fun save(uid: Long, userEnvelopeStatistic: UserEnvelopeStatistic) {
        return withMDCContext(Dispatchers.IO) {
            userEnvelopeStatisticRepository.save(
                uid,
                userEnvelopeStatistic
            )
        }
    }

    suspend fun getStatisticOrNull(uid: Long): UserEnvelopeStatistic? {
        return withMDCContext(Dispatchers.IO) { userEnvelopeStatisticRepository.getStatistic(uid) }
    }
}
