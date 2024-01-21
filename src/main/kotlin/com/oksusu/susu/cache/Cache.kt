package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.SUSU_BASIC_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.extension.toTypeReference
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.domain.UserStatistic
import java.time.Duration

class Cache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
    val duration: Duration,
) {
    companion object Factory {
        fun getSusuBasicStatisticCache(): Cache<SusuBasicStatistic> {
            return Cache(
                key = SUSU_BASIC_STATISTIC_KEY,
                type = SusuBasicStatistic::class.java.toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        val getSusuBasicStatisticCache: Factory.() -> Cache<SusuBasicStatistic> =
            { getSusuBasicStatisticCache() }

        fun getSusuSpecificStatisticCache(key: String): Cache<String> {
            return Cache(
                key = key,
                type = String::class.java.toTypeReference(),
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }

        fun getUserStatisticCache(key: String): Cache<UserStatistic> {
            return Cache(
                key = key,
                type = UserStatistic::class.java.toTypeReference(),
                duration = Duration.ofSeconds(USER_STATISTIC_TTL)
            )
        }
    }
}
