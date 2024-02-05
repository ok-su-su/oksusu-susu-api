package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.getOrNull
import com.oksusu.susu.cache.CacheService.Companion.set
import com.oksusu.susu.statistic.domain.SusuEnvelopeStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: SusuEnvelopeStatistic) {
        cacheService.set(
            cache = Cache.getSusuEnvelopeStatisticCache,
            value = value
        )
    }

    suspend fun getStatistic(): SusuEnvelopeStatistic? {
        return cacheService.getOrNull(cache = Cache.getSusuEnvelopeStatisticCache)
    }
}
