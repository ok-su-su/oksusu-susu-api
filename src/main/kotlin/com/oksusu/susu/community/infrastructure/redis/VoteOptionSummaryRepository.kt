package com.oksusu.susu.community.infrastructure.redis

import arrow.core.continuations.option
import com.oksusu.susu.community.domain.VoteOptionSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

const val VOTE_OPTION_SUMMARY_KEY = "vote_option_summary"

@Repository
class VoteOptionSummaryRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    suspend fun saveAll(voteOptionSummaries: List<VoteOptionSummary>) {
        // value : voteOptionId, score : count
        val tuples: ArrayList<TypedTuple<String>> = arrayListOf()
        voteOptionSummaries.forEach { option ->
            tuples.add(
                DefaultTypedTuple(
                    option.voteOptionId.toString(),
                    option.count.toDouble()
                )
            )
        }
        withContext(Dispatchers.IO) {
            reactiveRedisTemplate.opsForZSet().addAll(VOTE_OPTION_SUMMARY_KEY, tuples).awaitSingle()
        }
    }

    suspend fun findAllByVoteOptionIdIn(ids: List<Long>): List<VoteOptionSummary> {
        val strIds = ids.map { it.toString() }.toTypedArray()
        return withContext(Dispatchers.IO) {
            reactiveRedisTemplate.opsForZSet().score(VOTE_OPTION_SUMMARY_KEY, *strIds).awaitSingle()
        }.mapIndexed { idx, value -> VoteOptionSummary(ids[idx], value.toInt()) }
    }

}