package com.oksusu.susu.post.infrastructure.redis

import com.oksusu.susu.cache.CacheService
import com.oksusu.susu.cache.CacheService.Companion.zDeleteByMember
import com.oksusu.susu.cache.CacheService.Companion.zGetByMemberOrNull
import com.oksusu.susu.cache.CacheService.Companion.zGetByRange
import com.oksusu.susu.cache.CacheService.Companion.zSet
import com.oksusu.susu.cache.ZSetCache
import com.oksusu.susu.cache.model.ZSetModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.post.domain.vo.VoteSummary
import org.springframework.data.domain.Range
import org.springframework.stereotype.Repository

@Repository
class VoteSummaryRepository(
    private val cacheService: CacheService,
) {
    suspend fun save(summary: VoteSummary) {
        // value : postId, score : count
        cacheService.zSet(
            cache = ZSetCache.createVoteSummaryCache,
            member = summary.postId,
            score = summary.count.toDouble()
        )
    }

    suspend fun findByPostId(postId: Long): Double {
        return cacheService.zGetByMemberOrNull(
            cache = ZSetCache.createVoteSummaryCache,
            member = postId
        ) ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_SUMMARY_ERROR)
    }

    suspend fun findTopByCountOrderByCountDesc(size: Long): List<ZSetModel<Long>> {
        val range = Range.leftOpen(-size, -1L)
        return cacheService.zGetByRange(
            cache = ZSetCache.createVoteSummaryCache,
            range = range
        )
    }

    suspend fun findAllByCountBetween(from: Int, to: Int): List<ZSetModel<Long>> {
        val range = Range.leftOpen(-to.toLong(), -from.toLong())
        return cacheService.zGetByRange(
            cache = ZSetCache.createVoteSummaryCache,
            range = range
        )
    }

    suspend fun deleteByPostId(postId: Long) {
        cacheService.zDeleteByMember(
            cache = ZSetCache.createVoteSummaryCache,
            member = postId
        )
    }
}
