package com.oksusu.susu.api.post.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.domain.post.infrastructure.repository.model.VoteOptionAndCountModel
import kotlinx.coroutines.Dispatchers
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoteOptionService(
    private val voteOptionRepository: VoteOptionRepository,
) {
    @Transactional
    fun saveAllSync(voteOptions: List<VoteOption>): List<VoteOption> {
        return voteOptionRepository.saveAll(voteOptions)
    }

    suspend fun getOptionsByPostIdIn(postIds: List<Long>): List<VoteOption> {
        return withMDCContext(Dispatchers.IO) {
            voteOptionRepository.findAllByPostIdInOrderBySeq(postIds)
        }
    }

    suspend fun getVoteOptions(postId: Long): List<VoteOption> {
        return withMDCContext(Dispatchers.IO) {
            voteOptionRepository.findAllByPostIdOrderBySeq(postId)
        }
    }

    suspend fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel> {
        return withMDCContext(Dispatchers.IO) {
            voteOptionRepository.getOptionAndCount(postId)
        }
    }

    suspend fun findByIdOrNull(optionId: Long): VoteOption? {
        return withMDCContext(Dispatchers.IO) {
            voteOptionRepository.findByIdOrNull(optionId)
        }
    }

    suspend fun findByIdOrThrow(optionId: Long): VoteOption {
        return findByIdOrNull(optionId) ?: throw NotFoundException(ErrorCode.NOT_FOUND_VOTE_OPTION_ERROR)
    }

    suspend fun validateCorrespondWithVote(postId: Long, optionId: Long) {
        findByIdOrThrow(optionId).takeIf { option -> option.postId == postId }
            ?: throw InvalidRequestException(ErrorCode.INVALID_VOTE_OPTION_ERROR)
    }
}
