package com.oksusu.susu.community.infrastructure.redis

import com.oksusu.susu.community.domain.vo.VoteOptionSummary
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Repository

const val VOTE_OPTION_SUMMARY_KEY = "vote_option_summary"

@Repository
class VoteOptionSummaryRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    suspend fun saveAll(
        voteOptionSummaries: List<VoteOptionSummary>,
        tuples: ArrayList<TypedTuple<String>>
    ) {
        // value : voteOptionId, score : count
        reactiveRedisTemplate.opsForZSet().addAll(VOTE_OPTION_SUMMARY_KEY, tuples).awaitSingle()
    }

    suspend fun findAllByVoteOptionIdIn(ids: Array<String>): List<Double> {
        return reactiveRedisTemplate.opsForZSet().score(VOTE_OPTION_SUMMARY_KEY, *ids).awaitSingle()
    }

    suspend fun save(voteOptionSummary: VoteOptionSummary) {
        // value : voteOptionId, score : count
        reactiveRedisTemplate.opsForZSet()
            .add(VOTE_OPTION_SUMMARY_KEY, voteOptionSummary.voteOptionId.toString(), voteOptionSummary.count.toDouble())
            .awaitSingle()
    }

    suspend fun findByVoteOptionId(voteOptionId: Long): Double {
        return reactiveRedisTemplate.opsForZSet()
            .score(VOTE_OPTION_SUMMARY_KEY, voteOptionId.toString()).awaitSingle()
    }
}