package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.infrastructure.repository.VoteHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoteHistoryService(
    private val voteHistoryRepository: VoteHistoryRepository,
) {
    @Transactional
    fun saveSync(voteHistory: VoteHistory): VoteHistory {
        return voteHistoryRepository.save(voteHistory)
    }

    suspend fun validateVoteExist(uid: Long, postId: Long, optionId: Long) {
        if (!existsByUidAndPostIdAndVoteOptionId(uid, postId, optionId)) {
            throw InvalidRequestException(ErrorCode.NOT_FOUND_VOTE_ERROR)
        }
    }

    suspend fun validateVoteNotExist(uid: Long, postId: Long) {
        if (existsByUidAndPostId(uid, postId)) {
            throw InvalidRequestException(ErrorCode.DUPLICATED_VOTE_ERROR)
        }
    }

    suspend fun existsByUidAndPostId(uid: Long, postId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            voteHistoryRepository.existsByUidAndPostId(uid, postId)
        }
    }

    suspend fun existsByUidAndPostIdAndVoteOptionId(uid: Long, postId: Long, optionId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            voteHistoryRepository.existsByUidAndPostIdAndVoteOptionId(uid, postId, optionId)
        }
    }

    @Transactional
    fun deleteByUidAndPostId(uid: Long, postId: Long) {
        voteHistoryRepository.deleteByUidAndPostId(uid, postId)
    }
}
