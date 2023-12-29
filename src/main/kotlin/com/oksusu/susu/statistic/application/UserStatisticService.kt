package com.oksusu.susu.statistic.application

import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.infrastructure.redis.UserStatisticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserStatisticService(
    private val userStatisticRepository: UserStatisticRepository,
) {
    suspend fun save(userStatistic: UserStatistic): UserStatistic {
        return withContext(Dispatchers.IO) { userStatisticRepository.save(userStatistic) }
    }

    suspend fun findByIdOrNull(id: Long): UserStatistic? {
        return withContext(Dispatchers.IO) {
            userStatisticRepository.findByIdOrNull(id)
        }
    }
}
