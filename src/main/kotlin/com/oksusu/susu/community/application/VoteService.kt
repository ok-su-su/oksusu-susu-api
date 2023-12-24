package com.oksusu.susu.community.application

import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.infrastructure.repository.CommunityRepository
import com.oksusu.susu.community.infrastructure.repository.model.CommunityAndVoteOptionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteService(
    private val communityService: CommunityService,
    private val communityRepository: CommunityRepository,
) {
    suspend fun getAllVotes(sliceRequest: SusuSliceRequest): Slice<Community> {
        return communityService.findAllByIsActiveAndTypeOrderByCreatedAtDes(
            true,
            CommunityType.VOTE,
            sliceRequest.toDefault()
        )
    }

    suspend fun getVote(id: Long): Community {
        return communityService.findByIdAndIsActiveAndTypeOrThrow(id, true, CommunityType.VOTE)
    }

    suspend fun getVoteAndOptions(id: Long): List<CommunityAndVoteOptionModel> {
        return withContext(Dispatchers.IO){
            communityRepository.getVoteAndOptions(id)
        }
    }

}