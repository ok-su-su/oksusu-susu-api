package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.SUSU_BASIC_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.common.util.toTypeReference
import com.oksusu.susu.statistic.domain.SusuBasicEnvelopeStatistic
import com.oksusu.susu.statistic.domain.UserEnvelopeStatistic
import java.time.Duration

class Cache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
    val duration: Duration,
) {
    companion object Factory {
        fun getSusuBasicStatisticCache(): Cache<SusuBasicEnvelopeStatistic> {
            return Cache(
                key = SUSU_BASIC_STATISTIC_KEY,
                type = toTypeReference<SusuBasicEnvelopeStatistic>(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        val getSusuBasicEnvelopeStatisticCache: Factory.() -> Cache<SusuBasicEnvelopeStatistic> =
            { getSusuBasicStatisticCache() }

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
