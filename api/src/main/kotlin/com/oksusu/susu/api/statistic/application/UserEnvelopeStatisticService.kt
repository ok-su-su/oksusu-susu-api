package com.oksusu.susu.api.statistic.application

import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.api.statistic.infrastructure.redis.UserEnvelopeStatisticRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class UserEnvelopeStatisticService(
    private val userEnvelopeStatisticRepository: UserEnvelopeStatisticRepository,
) {
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
