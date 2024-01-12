package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.VOTE_OPTION_SUMMARY_KEY
import com.oksusu.susu.common.consts.VOTE_SUMMARY_KEY

sealed class ZSetCache<VALUE_TYPE>(
    open val key: String,
    open val type: TypeReference<VALUE_TYPE>,
) {
    companion object Factory {
        val createVoteOptionSummaryCache: Factory.() -> ZSetCache<Long> =
            { VoteOptionSummaryCache.getCache() }

        val createVoteSummaryCache: Factory.() -> ZSetCache<Long> =
            { VoteSummaryCache.getCache() }
    }
}

class VoteOptionSummaryCache(
    override val key: String,
    override val type: TypeReference<Long>,
) : ZSetCache<Long>(key, type) {
    companion object Factory {
        fun getCache(): ZSetCache<Long> {
            return VoteOptionSummaryCache(
                key = VOTE_OPTION_SUMMARY_KEY,
                type = object : TypeReference<Long>() {}
            )
        }
    }
}

class VoteSummaryCache(
    override val key: String,
    override val type: TypeReference<Long>,
) : ZSetCache<Long>(key, type) {
    companion object Factory {
        fun getCache(): ZSetCache<Long> {
            return VoteSummaryCache(
                key = VOTE_SUMMARY_KEY,
                type = object : TypeReference<Long>() {}
            )
        }
    }
}
