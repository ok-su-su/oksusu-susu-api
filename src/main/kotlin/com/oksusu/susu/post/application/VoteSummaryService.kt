package com.oksusu.susu.post.application

import com.oksusu.susu.post.domain.vo.VoteSummary
import com.oksusu.susu.post.infrastructure.redis.VoteSummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class VoteSummaryService(
    private val voteSummaryRepository: VoteSummaryRepository,
) {
    private val logger = mu.KotlinLogging.logger { }

    suspend fun save(voteSummary: VoteSummary) {
        withContext(Dispatchers.IO) {
            voteSummaryRepository.save(voteSummary)
        }
    }

    suspend fun getSummaryByPostId(postId: Long): VoteSummary {
        return withContext(Dispatchers.IO) {
            voteSummaryRepository.findByPostId(postId)
        }.run { VoteSummary(postId = postId, count = this.toInt()) }
    }

    suspend fun increaseCount(postId: Long) {
        val summary = getSummaryByPostId(postId).apply { count++ }
        save(summary)
    }

    suspend fun decreaseCount(postId: Long) {
        val summary = getSummaryByPostId(postId).apply { count-- }
        save(summary)
    }

    suspend fun getPopularVotes(size: Long): List<VoteSummary> {
        return withContext(Dispatchers.IO) {
            voteSummaryRepository.findTopByCountOrderByCountDesc(size)
        }.map { VoteSummary(postId = it.value!!.toLong(), count = it.score!!.toInt()) }
            .toList().filter { it.count != 0 }.reversed()
    }

    suspend fun getSummaryBetween(from: Int, to: Int): List<VoteSummary> {
        // zrange 맨 뒤부터 가져올려면, 마지막이 -1이 되어야 합니다.
        val start = from.takeIf { from != 0 } ?: 1

        // category sorting 조건 때문에 50개 더 가져옵니다.
        // score 역순으로 결과값이 나와서 내림차순으로 정렬합니다.
        return withContext(Dispatchers.IO) {
            voteSummaryRepository.findAllByCountBetween(start, to + 50)
        }.map { VoteSummary(postId = it.value!!.toLong(), count = it.score!!.toInt()) }
            .toList().reversed()
    }

    suspend fun deleteSummaryByPostId(postId: Long) {
        withContext(Dispatchers.IO) {
            voteSummaryRepository.deleteByPostId(postId)
        }
    }
}
