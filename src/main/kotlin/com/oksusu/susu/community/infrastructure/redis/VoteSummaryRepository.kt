package com.oksusu.susu.community.infrastructure.redis

import com.oksusu.susu.community.domain.vo.VoteSummary
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository

const val VOTE_SUMMARY_KEY = "vote_summary"

@Repository
class VoteSummaryRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    suspend fun save(voteSummary: VoteSummary) {
        // value : communityId, score : count
        reactiveRedisTemplate.opsForZSet()
            .add(VOTE_SUMMARY_KEY, voteSummary.communityId.toString(), voteSummary.count.toDouble()).awaitSingle()
    }

    suspend fun findByCommunityId(communityId: Long): Double {
        return reactiveRedisTemplate.opsForZSet()
            .score(VOTE_SUMMARY_KEY, communityId.toString()).awaitSingle()
    }

}