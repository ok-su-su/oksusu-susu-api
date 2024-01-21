package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import org.springframework.stereotype.Repository

@Repository
class SusuSpecificStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun findByKey(key: String): String? {
        return cacheService.getOrNull(Cache.getSusuSpecificStatisticCache(key))
    }

    suspend fun save(key: String, value: String) {
        cacheService.set(Cache.getSusuSpecificStatisticCache(key), value)
    }
}
