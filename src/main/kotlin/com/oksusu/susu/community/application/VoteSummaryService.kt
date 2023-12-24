package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.vo.VoteSummary
import com.oksusu.susu.community.infrastructure.redis.VoteSummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class VoteSummaryService(
    private val voteSummaryRepository: VoteSummaryRepository,
) {
    suspend fun save(voteSummary: VoteSummary) {
        withContext(Dispatchers.IO) {
            voteSummaryRepository.save(voteSummary)
        }
    }

    suspend fun getSummaryByCommunityId(communityId: Long): VoteSummary {
        return withContext(Dispatchers.IO) {
            voteSummaryRepository.findByCommunityId(communityId)
        }.run { VoteSummary(communityId = communityId, count = this.toInt()) }
    }

    suspend fun increaseCount(communityId: Long) {
        val summary = getSummaryByCommunityId(communityId).apply { count++ }
        save(summary)
    }

    suspend fun decreaseCount(communityId: Long) {
        val summary = getSummaryByCommunityId(communityId).apply { count-- }
        save(summary)
    }
}