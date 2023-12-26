package com.oksusu.susu.community.infrastructure.redis

import com.oksusu.susu.community.domain.vo.VoteSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Range
import org.springframework.data.domain.Range.Bound
import org.springframework.data.domain.Range.RangeBuilder
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Repository

const val VOTE_SUMMARY_KEY = "vote_summary"

@Repository
class VoteSummaryRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
) {
    val zSetOps = reactiveRedisTemplate.opsForZSet()

    suspend fun save(voteSummary: VoteSummary) {
        // value : communityId, score : count
        zSetOps
            .add(VOTE_SUMMARY_KEY, voteSummary.communityId.toString(), voteSummary.count.toDouble()).awaitSingle()
    }

    suspend fun findByCommunityId(communityId: Long): Double {
        return zSetOps
            .score(VOTE_SUMMARY_KEY, communityId.toString()).awaitSingle()
    }

    suspend fun findTop5ByCountOrderByCountDesc(): Flow<ZSetOperations.TypedTuple<String>> {
        val range = Range.leftOpen(-5L, -1L)
        return zSetOps
            .rangeWithScores(VOTE_SUMMARY_KEY, range)
            .asFlow()
    }
}
