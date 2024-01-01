package com.oksusu.susu.statistic.infrastructure.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.extension.toJson
import com.oksusu.susu.statistic.domain.UserStatistic
import org.springframework.stereotype.Repository

@Repository
class UserStatisticRepository(
    private val cacheService: CacheService,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    suspend fun save(uid: Long, statistic: UserStatistic) {
        val value = statistic.toJson()
        cacheService.save(cacheKeyGenerateHelper.getUserStatisticKey(uid), value, USER_STATISTIC_TTL)
    }

    suspend fun getStatistic(uid: Long): UserStatistic? {
        return cacheService.findByKey(cacheKeyGenerateHelper.getUserStatisticKey(uid))?.let {
            jacksonObjectMapper().readValue(it, UserStatistic::class.java)
        }
    }
}
