package com.oksusu.susu.statistic.infrastructure.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.extension.toJson
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuBasicStatisticRepository(
    private val cacheService: CacheService,
) {
    companion object {
        const val SUSU_BASIC_STATISTIC_KEY = "susu_basic_statistic"
    }

    suspend fun save(statistic: SusuBasicStatistic) {
        val value = statistic.toJson()
        cacheService.save(SUSU_BASIC_STATISTIC_KEY, value)
    }

    suspend fun getStatistic(): SusuBasicStatistic? {
        return cacheService.findByKey(SUSU_BASIC_STATISTIC_KEY)?.let {
            jacksonObjectMapper().readValue(it, SusuBasicStatistic::class.java)
        }
    }
}
