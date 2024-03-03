package com.oksusu.susu.statistic.application

import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import com.oksusu.susu.statistic.infrastructure.redis.UserEnvelopeStatisticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class UserEnvelopeStatisticService(
    private val userEnvelopeStatisticRepository: UserEnvelopeStatisticRepository,
) {
    suspend fun save(uid: Long, userEnvelopeStatistic: UserEnvelopeStatistic) {
        return withContext(Dispatchers.IO.withMDCContext()) {
            userEnvelopeStatisticRepository.save(
                uid,
                userEnvelopeStatistic
            )
        }
    }

    suspend fun getStatisticOrNull(uid: Long): UserEnvelopeStatistic? {
        return withContext(Dispatchers.IO.withMDCContext()) { userEnvelopeStatisticRepository.getStatistic(uid) }
    }
}
