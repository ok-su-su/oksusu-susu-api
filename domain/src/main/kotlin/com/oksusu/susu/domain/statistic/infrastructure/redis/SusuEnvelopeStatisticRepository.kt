package com.oksusu.susu.domain.statistic.infrastructure.redis

import com.oksusu.susu.cache.Cache
import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.getOrNull
import com.oksusu.susu.cache.CacheService.Companion.set
import com.oksusu.susu.cache.model.SusuEnvelopeStatisticCacheModel
import com.oksusu.susu.domain.statistic.domain.SusuEnvelopeStatistic
import org.springframework.stereotype.Repository

@Repository
class SusuEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(value: SusuEnvelopeStatistic) {
        val cacheModel = SusuEnvelopeStatisticCacheModel(
            recentSpent = value.recentSpent,
            mostSpentMonth = value.mostSpentMonth,
            mostFrequentRelationShip = value.mostFrequentRelationShip,
            mostFrequentCategory = value.mostFrequentCategory
        )

        cacheService.set(
            cache = Cache.getSusuEnvelopeStatisticCache,
            value = cacheModel
        )
    }

    suspend fun getStatistic(): SusuEnvelopeStatistic? {
        val cacheModel = cacheService.getOrNull(cache = Cache.getSusuEnvelopeStatisticCache)

        return cacheModel?.run {
            SusuEnvelopeStatistic(
                recentSpent = this.recentSpent,
                mostSpentMonth = this.mostSpentMonth,
                mostFrequentRelationShip = this.mostFrequentRelationShip,
                mostFrequentCategory = this.mostFrequentCategory
            )
        }
    }
}
