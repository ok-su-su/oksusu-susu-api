package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.VoteOption
import com.oksusu.susu.community.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.community.model.VoteOptionModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun validateSeq(optionModels: List<VoteOptionModel>) {
        optionModels.map { option -> option.seq }.toSet().count { seq -> seq > 0 }.run {
            if (this != optionModels.size) {
                throw InvalidRequestException(ErrorCode.INVALID_VOTE_OPTION_SEQUENCE)
            }
        }
    }

    suspend fun getOptionsByCommunityIdIn(communityIds: List<Long>): List<VoteOption> {
        return withContext(Dispatchers.IO) {
            voteOptionRepository.findAllByCommunityIdInOrderBySeq(communityIds)
        }
    }

    suspend fun getVoteOptions(communityId: Long): List<VoteOption> {
        return withContext(Dispatchers.IO) {
            voteOptionRepository.findAllByCommunityIdOrderBySeq(communityId)
        }
    }
}
