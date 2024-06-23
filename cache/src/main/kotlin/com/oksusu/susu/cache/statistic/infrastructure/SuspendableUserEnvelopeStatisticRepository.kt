package com.oksusu.susu.cache.statistic.infrastructure

import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.key.CacheKeyGenerateHelper
import com.oksusu.susu.cache.model.UserEnvelopeStatisticCacheModel
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.cache.statistic.domain.UserEnvelopeStatistic
import org.springframework.stereotype.Repository


@Repository
class SuspendableUserEnvelopeStatisticRepository(
    private val cacheService: CacheService,
) : UserEnvelopeStatisticRepository {
    override suspend fun save(uid: Long, value: UserEnvelopeStatistic) {
        val key = CacheKeyGenerateHelper.getUserStatisticKey(uid)

        val cacheModel = UserEnvelopeStatisticCacheModel(
            recentSpent = value.recentSpent,
            mostSpentMonth = value.mostSpentMonth,
            mostFrequentRelationShip = value.mostFrequentRelationShip,
            mostFrequentCategory = value.mostFrequentCategory,
            maxReceivedEnvelope = value.maxReceivedEnvelope,
            maxSentEnvelope = value.maxSentEnvelope
        )

        cacheService.set(Cache.getUserEnvelopeStatisticCache(key), cacheModel)
    }

    override suspend fun getStatistic(uid: Long): UserEnvelopeStatistic? {
        val key = CacheKeyGenerateHelper.getUserStatisticKey(uid)

        val cacheModel = cacheService.getOrNull(Cache.getUserEnvelopeStatisticCache(key))

        return cacheModel?.run {
            UserEnvelopeStatistic(
                recentSpent = this.recentSpent,
                mostSpentMonth = this.mostSpentMonth,
                mostFrequentRelationShip = this.mostFrequentRelationShip,
                mostFrequentCategory = this.mostFrequentCategory,
                maxReceivedEnvelope = this.maxReceivedEnvelope,
                maxSentEnvelope = this.maxSentEnvelope
            )
        }
    }
}
