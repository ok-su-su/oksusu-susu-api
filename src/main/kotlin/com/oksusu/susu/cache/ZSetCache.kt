package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.oksusu.susu.common.consts.VOTE_OPTION_SUMMARY_KEY
import com.oksusu.susu.common.consts.VOTE_SUMMARY_KEY
import com.oksusu.susu.extension.toTypeReference

class ZSetCache<VALUE_TYPE>(
    val key: String,
    val type: TypeReference<VALUE_TYPE>,
) {
    companion object Factory {
        fun getVoteOptionSummaryCache(): ZSetCache<Long> {
            return ZSetCache(
                key = VOTE_OPTION_SUMMARY_KEY,
                type = Long::class.java.toTypeReference()
            )
        }

        val getVoteOptionSummaryCache: Factory.() -> ZSetCache<Long> =
            { getVoteOptionSummaryCache() }

        fun getVoteSummaryCache(): ZSetCache<Long> {
            return ZSetCache(
                key = VOTE_SUMMARY_KEY,
                type = Long::class.java.toTypeReference()
            )
        }

        val getVoteSummaryCache: Factory.() -> ZSetCache<Long> =
            { getVoteSummaryCache() }
    }
}
