package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.VoteOptionSummary
import com.oksusu.susu.community.infrastructure.redis.VoteOptionSummaryRepository
import org.springframework.stereotype.Service

@Service
class VoteOptionSummaryService (
    private val voteOptionSummaryRepository: VoteOptionSummaryRepository,
){
    suspend fun saveAll(voteOptionSummaries: List<VoteOptionSummary>) {
        voteOptionSummaryRepository.saveAll(voteOptionSummaries)
    }

    suspend fun getSummariesByOptionIdIn(ids: List<Long>): List<VoteOptionSummary> {
        return voteOptionSummaryRepository.findAllByVoteOptionIdIn(ids)
    }
}