package com.oksusu.susu.community.infrastructure.redis

import com.oksusu.susu.community.domain.VoteSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

const val VOTE_SUMMARY_KEY = "vote_summary"

@Repository
class VoteSummaryRepository(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
    suspend fun save(voteSummary: VoteSummary) {
        // value : communityId, score : count
        withContext(Dispatchers.IO) {
            reactiveRedisTemplate.opsForZSet()
                .add(VOTE_SUMMARY_KEY, voteSummary.communityId.toString(), voteSummary.count.toDouble()).awaitSingle()
        }
    }

    suspend fun findByCommunityId(communityId: Long): VoteSummary {
        return withContext(Dispatchers.IO) {
            reactiveRedisTemplate.opsForZSet()
                .score(VOTE_SUMMARY_KEY, communityId.toString()).awaitSingle()
        }.run { VoteSummary(communityId = communityId, count = this.toInt()) }
    }

}