package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.VoteHistory
import com.oksusu.susu.community.infrastructure.repository.VoteHistoryRepository
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoteHistoryService(
    private val voteHistoryRepository: VoteHistoryRepository
) {
    @Transactional
    fun saveSync(voteHistory: VoteHistory): VoteHistory {
        return voteHistoryRepository.save(voteHistory)
    }

    suspend fun validateVoteExist(uid: Long, communityId: Long, optionId: Long) {
        if (!existsByUidAndCommunityIdAndVoteOptionId(uid, communityId, optionId)) {
            throw InvalidRequestException(ErrorCode.NOT_FOUND_VOTE_ERROR)
        }
    }

    suspend fun validateVoteNotExist(uid: Long, communityId: Long) {
        if (existsByUidAndCommunityId(uid, communityId)) {
            throw InvalidRequestException(ErrorCode.DUPLICATED_VOTE_ERROR)
        }
    }

    suspend fun existsByUidAndCommunityId(uid: Long, communityId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            voteHistoryRepository.existsByUidAndCommunityId(uid, communityId)
        }
    }

    suspend fun existsByUidAndCommunityIdAndVoteOptionId(uid: Long, communityId: Long, optionId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            voteHistoryRepository.existsByUidAndCommunityIdAndVoteOptionId(uid, communityId, optionId)
        }
    }

    @Transactional
    fun deleteByUidAndCommunityId(uid: Long, communityId: Long) {
        voteHistoryRepository.deleteByUidAndCommunityId(uid, communityId)
    }
}