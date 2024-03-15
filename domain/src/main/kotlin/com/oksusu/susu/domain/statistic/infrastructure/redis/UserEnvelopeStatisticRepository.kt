package com.oksusu.susu.domain.statistic.infrastructure.redis

import com.oksusu.susu.domain.cache.Cache
import com.oksusu.susu.domain.cache.CacheService
import com.oksusu.susu.domain.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.domain.statistic.domain.UserEnvelopeStatistic
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
