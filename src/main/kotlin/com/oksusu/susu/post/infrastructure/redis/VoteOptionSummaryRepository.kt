package com.oksusu.susu.post.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.post.domain.vo.VoteOptionSummary
import org.springframework.stereotype.Repository

const val VOTE_OPTION_SUMMARY_KEY = "vote_option_summary"

@Repository
class VoteOptionSummaryRepository(
    private val cacheService: CacheService,
) {
    suspend fun <T> saveAll(
        tuples: Map<T, Long>,
    ) {
        // value : voteOptionId, score : count
        cacheService.zSetSaveAll(VOTE_OPTION_SUMMARY_KEY, tuples)
    }

    suspend fun findAllByVoteOptionIdIn(ids: List<Long>): List<Double> {
        val strIds = ids.map { it.toString() }
        return cacheService.zSetFindByMemberIn(VOTE_OPTION_SUMMARY_KEY, strIds)
    }

    suspend fun save(summary: VoteOptionSummary) {
        // value : voteOptionId, score : count
        cacheService.zSetSave(VOTE_OPTION_SUMMARY_KEY, mapOf(summary.voteOptionId to summary.count.toLong()))
    }

    suspend fun findByVoteOptionId(voteOptionId: Long): Double {
        return cacheService.zSetFindByMember(VOTE_OPTION_SUMMARY_KEY, voteOptionId.toString())
    }

    suspend fun deleteByVoteOptionIdIn(ids: List<Long>) {
        val strIds = ids.map { it.toString() }
        return cacheService.zSetDeleteByMemberIn(VOTE_OPTION_SUMMARY_KEY, strIds)
    }
}
