package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import org.springframework.stereotype.Repository

@Repository
class SusuSpecificStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun findByKey(key: String): String? {
        return cacheService.findByKey(key)
    }

    suspend fun save(key: String, value: String) {
        cacheService.save(key, value)
    }
}
