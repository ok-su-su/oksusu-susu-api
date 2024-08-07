package com.oksusu.susu.cache.key

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.cache.model.*
import com.oksusu.susu.common.consts.*
import com.oksusu.susu.common.util.toTypeReference
import java.time.Duration
import java.time.LocalDateTime

data class Cache<VALUE_TYPE>(
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

        fun getUserReportCountCache(): Cache<Map<Long, Long>> {
            return Cache(
                key = USER_REPORT_COUNT_KEY,
                type = toTypeReference(),
                duration = Duration.ofDays(2)
            )
        }

        fun getPostReportCountCache(): Cache<Map<Long, Long>> {
            return Cache(
                key = POST_REPORT_COUNT_KEY,
                type = toTypeReference(),
                duration = Duration.ofDays(2)
            )
        }

        fun getUserCommunityPunishedCountCache(): Cache<Map<Long, Long>> {
            return Cache(
                key = USER_COMMUNITY_PUNISHED_COUNT_KEY,
                type = toTypeReference(),
                duration = Duration.ofDays(2)
            )
        }

        fun getSusuEnvelopeStatisticAmountCache(): Cache<Map<String, Long>> {
            return Cache(
                key = SUSU_ENVELOPE_STATISTIC_AMOUNT_KEY,
                type = toTypeReference(),
                duration = Duration.ofDays(1).plusHours(1)
            )
        }

        fun getFailedSentSlackMessageCache(time: LocalDateTime): Cache<FailedSentSlackMessageCache> {
            return Cache(
                key = "$FAILED_SENT_SLACK_MESSAGE_KEY:${time.minute}",
                type = toTypeReference(),
                duration = Duration.ofHours(2)
            )
        }

        fun getFailedSentDiscordMessageCache(time: LocalDateTime): Cache<FailedSentDiscordMessageCache> {
            return Cache(
                key = "$FAILED_SENT_DISCORD_MESSAGE_KEY:${time.minute}",
                type = toTypeReference(),
                duration = Duration.ofHours(2)
            )
        }
    }
}
