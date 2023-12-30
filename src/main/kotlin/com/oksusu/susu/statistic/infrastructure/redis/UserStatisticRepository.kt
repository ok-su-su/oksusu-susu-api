package com.oksusu.susu.statistic.infrastructure.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.extension.toJson
import com.oksusu.susu.statistic.domain.UserStatistic
import org.springframework.stereotype.Repository

@Repository
class UserStatisticRepository(
    private val cacheService: CacheService,
) {
    companion object {
        const val USER_STATISTIC_KEY = "user_statistic"
        const val USER_STATISTIC_TTL = 60 * 3
    }

    suspend fun save(uid: Long, statistic: UserStatistic) {
        val value = statistic.toJson()
        cacheService.save(getUserStatisticKey(uid), value, USER_STATISTIC_TTL)
    }

    suspend fun getStatistic(uid: Long): UserStatistic? {
        return cacheService.findByKey(getUserStatisticKey(uid))?.let {
            jacksonObjectMapper().readValue(it, UserStatistic::class.java)
        }
    }

    suspend fun getUserStatisticKey(uid: Long): String {
        return USER_STATISTIC_KEY + "_$uid"
    }
}
