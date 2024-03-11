package com.oksusu.susu.api.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.api.consts.SUSU_ENVELOPE_STATISTIC_KEY
import com.oksusu.susu.api.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.api.consts.USER_STATISTIC_TTL
import com.oksusu.susu.api.util.toTypeReference
import com.oksusu.susu.api.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.api.statistic.domain.UserEnvelopeStatistic
import java.time.Duration

class Cache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
    val duration: Duration,
) {
    companion object Factory {
        fun getSusuEnvelopeStatisticCache(): Cache<SusuEnvelopeStatistic> {
            return Cache(
                key = SUSU_ENVELOPE_STATISTIC_KEY,
                type = toTypeReference<SusuEnvelopeStatistic>(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        val getSusuEnvelopeStatisticCache: Factory.() -> Cache<SusuEnvelopeStatistic> =
            { getSusuEnvelopeStatisticCache() }

        fun getSusuSpecificStatisticCache(key: String): Cache<Long> {
            return Cache(
                key = key,
                type = toTypeReference<Long>(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        fun getUserEnvelopeStatisticCache(key: String): Cache<UserEnvelopeStatistic> {
            return Cache(
                key = key,
                type = toTypeReference<UserEnvelopeStatistic>(),
                duration = Duration.ofSeconds(USER_STATISTIC_TTL)
            )
        }

        fun getRefreshTokenCache(key: String, ttl: Long): Cache<String> {
            return Cache(
                key = key,
                type = toTypeReference<String>(),
                duration = Duration.ofSeconds(ttl)
            )
        }
    }
}
