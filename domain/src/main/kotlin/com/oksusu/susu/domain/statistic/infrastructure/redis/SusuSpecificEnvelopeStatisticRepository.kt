package com.oksusu.susu.domain.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import org.springframework.stereotype.Repository

@Repository
class SusuSpecificEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun findByKey(key: String): Long? {
        return cacheService.getOrNull(Cache.getSusuSpecificStatisticCache(key))
    }

    suspend fun save(key: String, value: Long) {
        cacheService.set(Cache.getSusuSpecificStatisticCache(key), value)
    }
}
