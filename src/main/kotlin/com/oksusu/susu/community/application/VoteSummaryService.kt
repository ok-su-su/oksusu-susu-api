package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.VoteSummary
import com.oksusu.susu.community.infrastructure.redis.VoteSummaryRepository
import org.springframework.stereotype.Service

@Service
class VoteSummaryService (
    private val voteSummaryRepository: VoteSummaryRepository,
){
    suspend fun save(voteSummary: VoteSummary) {
        voteSummaryRepository.save(voteSummary)
    }

    suspend fun getSummaryByCommunityId(communityId: Long): VoteSummary {
        return voteSummaryRepository.findByCommunityId(communityId)
    }
}