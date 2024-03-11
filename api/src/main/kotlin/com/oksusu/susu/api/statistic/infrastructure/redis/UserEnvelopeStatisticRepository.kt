package com.oksusu.susu.api.statistic.infrastructure.redis

import com.oksusu.susu.api.cache.Cache
import com.oksusu.susu.api.cache.CacheService
import com.oksusu.susu.api.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.api.statistic.domain.UserEnvelopeStatistic
import org.springframework.stereotype.Repository

@Repository
class UserEnvelopeStatisticRepository(
    private val cacheService: CacheService,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    suspend fun save(uid: Long, value: UserEnvelopeStatistic) {
        val key = cacheKeyGenerateHelper.getUserStatisticKey(uid)

        cacheService.set(Cache.getUserEnvelopeStatisticCache(key), value)
    }

    suspend fun getStatistic(uid: Long): UserEnvelopeStatistic? {
        val key = cacheKeyGenerateHelper.getUserStatisticKey(uid)

        return cacheService.getOrNull(Cache.getUserEnvelopeStatisticCache(key))
    }
}
