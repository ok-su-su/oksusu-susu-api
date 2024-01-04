package com.oksusu.susu.statistic.application

import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.infrastructure.redis.UserStatisticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class UserStatisticService(
    private val userStatisticRepository: UserStatisticRepository,
) {
    suspend fun save(uid: Long, userStatistic: UserStatistic) {
        return withContext(Dispatchers.IO) { userStatisticRepository.save(uid, userStatistic) }
    }

    suspend fun getStatisticOrNull(uid: Long): UserStatistic? {
        return withContext(Dispatchers.IO) { userStatisticRepository.getStatistic(uid) }
    }
}
