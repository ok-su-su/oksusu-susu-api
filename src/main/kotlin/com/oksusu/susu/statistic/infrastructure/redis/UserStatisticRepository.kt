package com.oksusu.susu.statistic.infrastructure.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.UserStatisticCache
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
    suspend fun save(uid: Long, value: UserStatistic) {
        val key = cacheKeyGenerateHelper.getUserStatisticKey(uid)

        cacheService.set(UserStatisticCache.getCache(key), value)
    }

    suspend fun getStatistic(uid: Long): UserStatistic? {
        val key = cacheKeyGenerateHelper.getUserStatisticKey(uid)

        return cacheService.getOrNull(UserStatisticCache.getCache(key))
    }
}
