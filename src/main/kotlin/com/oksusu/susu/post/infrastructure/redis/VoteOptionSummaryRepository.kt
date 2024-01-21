package com.oksusu.susu.post.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.zDeleteByMembers
import com.oksusu.susu.cache.CacheService.Companion.zGetByMemberOrNull
import com.oksusu.susu.cache.CacheService.Companion.zGetByMembers
import com.oksusu.susu.cache.CacheService.Companion.zSet
import com.oksusu.susu.cache.CacheService.Companion.zSetAll
import com.oksusu.susu.cache.ZSetCache
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.post.domain.vo.VoteOptionSummary
import org.springframework.stereotype.Repository

@Repository
class VoteOptionSummaryRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(summary: VoteOptionSummary) {
        // value : voteOptionId, score : count
        cacheService.zSet(
            cache = ZSetCache.getVoteOptionSummaryCache,
            member = summary.voteOptionId,
            score = summary.count.toDouble()
        )
    }

    suspend fun saveAll(tuples: Map<Long, Double>) {
        // value : voteOptionId, score : count
        cacheService.zSetAll(cache = ZSetCache.getVoteOptionSummaryCache, tuples = tuples)
    }

    suspend fun findAllByVoteOptionIdIn(ids: List<Long>): List<Double> {
        return cacheService.zGetByMembers(
            cache = ZSetCache.getVoteOptionSummaryCache,
            members = ids
        )
    }

    suspend fun findByVoteOptionId(voteOptionId: Long): Double {
        return cacheService.zGetByMemberOrNull(
            cache = ZSetCache.getVoteOptionSummaryCache,
            member = voteOptionId
        ) ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_OPTION_SUMMARY_ERROR)
    }

    suspend fun deleteByVoteOptionIdIn(ids: List<Long>) {
        return cacheService.zDeleteByMembers(
            cache = ZSetCache.getVoteOptionSummaryCache,
            members = ids
        )
    }
}
