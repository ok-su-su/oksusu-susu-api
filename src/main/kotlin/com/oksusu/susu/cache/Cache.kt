package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.SUSU_BASIC_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.domain.UserStatistic
import java.time.Duration

sealed class Cache<VALUE_TYPE>(
    open val key: String,
    open val type: TypeReference<VALUE_TYPE>,
    open val duration: Duration,
) {
    companion object Factory {
        val createSusuBasicStatisticCache: Factory.() -> Cache<SusuBasicStatistic> =
            { SusuBasicStatisticCache.getCache() }

        val createSusuSpecificStatisticCache: Factory.(key: String) -> Cache<String> =
            { key -> SusuSpecificStatisticCache.getCache(key) }

        val createUserStatisticCache: Factory.(key: String) -> Cache<UserStatistic> =
            { key -> UserStatisticCache.getCache(key) }
    }
}

class SusuBasicStatisticCache(
    override val key: String,
    override val type: TypeReference<SusuBasicStatistic>,
    override val duration: Duration,
) : Cache<SusuBasicStatistic>(key, type, duration) {
    companion object Factory {
        fun getCache(): Cache<SusuBasicStatistic> {
            return SusuBasicStatisticCache(
                key = SUSU_BASIC_STATISTIC_KEY,
                type = object : TypeReference<SusuBasicStatistic>() {},
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }
    }
}

class SusuSpecificStatisticCache(
    override val key: String,
    override val type: TypeReference<String>,
    override val duration: Duration,
) : Cache<String>(key, type, duration) {
    companion object Factory {
        fun getCache(key: String): Cache<String> {
            return SusuSpecificStatisticCache(
                key = key,
                type = object : TypeReference<String>() {},
                duration = Duration.ofSeconds(SUSU_STATISTIC_TTL)
            )
        }
    }
}

class UserStatisticCache(
    override val key: String,
    override val type: TypeReference<UserStatistic>,
    override val duration: Duration,
) : Cache<UserStatistic>(key, type, duration) {
    companion object Factory {
        fun getCache(key: String): Cache<UserStatistic> {
            return UserStatisticCache(
                key = key,
                type = object : TypeReference<UserStatistic>() {},
                duration = Duration.ofSeconds(USER_STATISTIC_TTL)
            )
        }
    }
}
