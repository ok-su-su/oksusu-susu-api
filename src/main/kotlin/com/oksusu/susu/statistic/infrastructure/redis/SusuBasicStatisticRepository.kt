package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.get
import com.oksusu.susu.cache.CacheService.Companion.getOrNull
import com.oksusu.susu.cache.CacheService.Companion.set
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuBasicStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: SusuBasicStatistic) {
        cacheService.set(
            cache = Cache.getSusuBasicStatisticCache,
            value = value
        )
    }

    suspend fun getStatistic(): SusuBasicStatistic? {
        return cacheService.getOrNull(cache = Cache.getSusuBasicStatisticCache)
    }
}
