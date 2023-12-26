package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.vo.VoteOptionSummary
import com.oksusu.susu.community.infrastructure.redis.VoteOptionSummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.DefaultTypedTuple
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service

@Service
class VoteOptionSummaryService(
    private val voteOptionSummaryRepository: VoteOptionSummaryRepository,
) {
    suspend fun saveAll(voteOptionSummaries: List<VoteOptionSummary>) {
        // value : voteOptionId, score : count
        val tuples = voteOptionSummaries.associate { option ->
            option.voteOptionId.toString() to option.count.toLong()
        }

        withContext(Dispatchers.IO) {
            voteOptionSummaryRepository.saveAll(tuples)
        }
    }

    suspend fun getSummariesByOptionIdIn(ids: List<Long>): List<VoteOptionSummary> {
        val strIds = ids.map { it.toString() }
        return withContext(Dispatchers.IO) {
            voteOptionSummaryRepository.findAllByVoteOptionIdIn(strIds)
        }.mapIndexed { idx, value -> VoteOptionSummary(ids[idx], value.toInt()) }
    }

    suspend fun save(voteOptionSummary: VoteOptionSummary) {
        withContext(Dispatchers.IO) {
            voteOptionSummaryRepository.save(voteOptionSummary)
        }
    }

    suspend fun getSummaryByOptionId(voteOptionId: Long): VoteOptionSummary {
        return withContext(Dispatchers.IO) {
            voteOptionSummaryRepository.findByVoteOptionId(voteOptionId)
        }.run { VoteOptionSummary(voteOptionId = voteOptionId, count = this.toInt()) }
    }

    suspend fun increaseCount(optionId: Long) {
        val summary = getSummaryByOptionId(optionId).apply { count++ }
        save(summary)
    }

    suspend fun decreaseCount(optionId: Long) {
        val summary = getSummaryByOptionId(optionId).apply { count-- }
        save(summary)
    }
}