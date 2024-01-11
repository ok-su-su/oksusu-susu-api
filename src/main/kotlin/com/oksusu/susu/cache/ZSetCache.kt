package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.SUSU_BASIC_STATISTIC_KEY
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.common.consts.USER_STATISTIC_TTL
import com.oksusu.susu.post.infrastructure.redis.VOTE_OPTION_SUMMARY_KEY
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.domain.UserStatistic
import java.time.Duration

sealed class ZSetCache<VALUE_TYPE>(
    open val key: String,
    open val type: TypeReference<VALUE_TYPE>,
) {
    companion object Factory {
    }
}

class VoteOptionSummaryCache(
    override val key: String,
    override val type: TypeReference<String>,
) : ZSetCache<String>(key, type) {
    companion object Factory {
        fun getCache(): ZSetCache<String> {
            return VoteOptionSummaryCache(
                key = VOTE_OPTION_SUMMARY_KEY,
                type = object : TypeReference<String>() {},
            )
        }
    }
}
