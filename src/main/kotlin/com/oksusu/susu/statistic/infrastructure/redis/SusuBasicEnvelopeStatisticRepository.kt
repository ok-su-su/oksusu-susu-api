package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.getOrNull
import com.oksusu.susu.cache.CacheService.Companion.set
import com.oksusu.susu.statistic.domain.SusuBasicEnvelopeStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuBasicEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: SusuBasicEnvelopeStatistic) {
        cacheService.set(
            cache = Cache.getSusuBasicEnvelopeStatisticCache,
            value = value
        )
    }

    suspend fun getStatistic(): SusuBasicEnvelopeStatistic? {
        return cacheService.getOrNull(cache = Cache.getSusuBasicEnvelopeStatisticCache)
    }
}
