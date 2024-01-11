package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.SusuSpecificStatisticCache
import org.springframework.stereotype.Repository

@Repository
class SusuSpecificStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun findByKey(key: String): String? {
        return cacheService.getOrNull(SusuSpecificStatisticCache.getCache(key))
    }

    suspend fun save(key: String, value: String) {
        cacheService.set(SusuSpecificStatisticCache.getCache(key), value)
    }
}
