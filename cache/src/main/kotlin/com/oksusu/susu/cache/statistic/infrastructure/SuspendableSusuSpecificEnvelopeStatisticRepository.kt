package com.oksusu.susu.cache.statistic.infrastructure

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import org.springframework.stereotype.Repository

@Repository
class SuspendableSusuSpecificEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) : SusuSpecificEnvelopeStatisticRepository {
    override suspend fun findByKey(key: String): Long? {
        return cacheService.getOrNull(Cache.getSusuSpecificStatisticCache(key))
    }

    override suspend fun save(key: String, value: Long) {
        cacheService.set(Cache.getSusuSpecificStatisticCache(key), value)
    }
}
