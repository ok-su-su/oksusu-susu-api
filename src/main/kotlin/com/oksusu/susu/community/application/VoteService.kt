package com.oksusu.susu.community.application

import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteService(
    private val communityService: CommunityService,
) {
    suspend fun getAllVotes(sliceRequest: SusuSliceRequest): Slice<Community> {
        return withContext(Dispatchers.IO) {
            communityService.findAllByIsActiveAndTypeOrderByCreatedAtDes(
                true,
                CommunityType.VOTE,
                sliceRequest.toDefault()
            )
        }
    }

    suspend fun getVote(id: Long): Community {
        return communityService.findByIdAndIsActiveAndTypeOrThrow(id, true, CommunityType.VOTE)
    }

}