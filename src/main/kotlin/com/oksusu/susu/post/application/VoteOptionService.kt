package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.post.infrastructure.repository.model.VoteOptionAndCountModel
import com.oksusu.susu.post.model.VoteOptionWithoutIdModel
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

    suspend fun getOptionsByPostIdIn(postIds: List<Long>): List<VoteOption> {
        return withContext(Dispatchers.IO) {
            voteOptionRepository.findAllByPostIdInOrderBySeq(postIds)
        }
    }

    suspend fun getVoteOptions(postId: Long): List<VoteOption> {
        return withContext(Dispatchers.IO) {
            voteOptionRepository.findAllByPostIdOrderBySeq(postId)
        }
    }

    suspend fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel> {
        return withContext(Dispatchers.IO) {
            voteOptionRepository.getOptionAndCount(postId)
        }
    }
}
