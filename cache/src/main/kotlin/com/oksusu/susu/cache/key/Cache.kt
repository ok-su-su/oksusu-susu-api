package com.oksusu.susu.cache.key

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.cache.model.OidcPublicKeysCacheModel
import com.oksusu.susu.cache.model.SusuEnvelopeStatisticCacheModel
import com.oksusu.susu.cache.model.UserEnvelopeStatisticCacheModel
import com.oksusu.susu.common.consts.APPLE_OIDC_PUBLIC_KEY_KEY
import com.oksusu.susu.common.consts.SUSU_ENVELOPE_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.common.util.toTypeReference
import java.time.Duration

class Cache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
    val duration: Duration,
) {
    companion object Factory {
        fun getSusuEnvelopeStatisticCache(): Cache<SusuEnvelopeStatisticCacheModel> {
            return Cache(
                key = SUSU_ENVELOPE_STATISTIC_KEY,
                type = toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        val getSusuEnvelopeStatisticCache: Factory.() -> Cache<SusuEnvelopeStatisticCacheModel> =
            { getSusuEnvelopeStatisticCache() }

        fun getSusuSpecificStatisticCache(key: String): Cache<Long> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        fun getUserEnvelopeStatisticCache(key: String): Cache<UserEnvelopeStatisticCacheModel> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(USER_STATISTIC_TTL)
            )
        }

        fun getRefreshTokenCache(key: String, ttl: Long): Cache<String> {
            return Cache(
                key = key,
                type = toTypeReference(),
                duration = Duration.ofSeconds(ttl)
            )
        }

        fun getAppleOidcPublicKeyCache(): Cache<OidcPublicKeysCacheModel> {
            return Cache(
                key = APPLE_OIDC_PUBLIC_KEY_KEY,
                type = toTypeReference(),
                duration = Duration.ofDays(7)
            )
        }
    }
}
