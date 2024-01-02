package com.oksusu.susu.post.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.post.domain.vo.VoteSummary
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository

const val VOTE_SUMMARY_KEY = "vote_summary"

@Repository
class VoteSummaryRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(summary: VoteSummary) {
        // value : postId, score : count
        cacheService.zSetSave(VOTE_SUMMARY_KEY, mapOf(summary.postId.toString() to summary.count.toLong()))
    }

    suspend fun findByPostId(postId: Long): Double {
        return cacheService.zSetFindByMember(VOTE_SUMMARY_KEY, postId.toString())
    }

    suspend fun findTopByCountOrderByCountDesc(size: Long): Flow<ZSetOperations.TypedTuple<String>> {
        val range = Range.leftOpen(-size, -1L)
        return cacheService.zSetFindRangeWithScores(VOTE_SUMMARY_KEY, range)
    }

    suspend fun findAllByCountBetween(from: Int, to: Int): Flow<ZSetOperations.TypedTuple<String>> {
        val range = Range.leftOpen(-to.toLong(), -from.toLong())
        return cacheService.zSetFindRangeWithScores(VOTE_SUMMARY_KEY, range)
    }

    suspend fun deleteByPostId(postId: Long) {
        cacheService.zSetDeleteByMemberIn(VOTE_SUMMARY_KEY, listOf(postId.toString()))
    }
}
