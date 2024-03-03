package com.oksusu.susu.post.application

import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.post.infrastructure.repository.model.VoteOptionAndCountModel
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
        return withContext(Dispatchers.IO.withMDCContext()) {
            voteOptionRepository.findAllByPostIdInOrderBySeq(postIds)
        }
    }

    suspend fun getVoteOptions(postId: Long): List<VoteOption> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            voteOptionRepository.findAllByPostIdOrderBySeq(postId)
        }
    }

    suspend fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            voteOptionRepository.getOptionAndCount(postId)
        }
    }
}
