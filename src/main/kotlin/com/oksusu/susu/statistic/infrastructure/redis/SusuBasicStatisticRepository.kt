package com.oksusu.susu.statistic.infrastructure.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.get
import com.oksusu.susu.cache.SusuBasicStatisticCache
import com.oksusu.susu.common.consts.SUSU_BASIC_STATISTIC_KEY
import com.oksusu.susu.extension.toJson
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuBasicStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: SusuBasicStatistic) {
        cacheService.set(SusuBasicStatisticCache.getCache(), value)
    }

    suspend fun getStatistic(): SusuBasicStatistic? {
        return cacheService.getOrNull(SusuBasicStatisticCache.getCache())
    }
}
