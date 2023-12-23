package com.oksusu.susu.community.application

import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.infrastructure.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunityService(
    private val communityRepository: CommunityRepository
) {
    @Transactional
    fun saveSync(community: Community): Community {
        return communityRepository.save(community)
    }

    suspend fun getAllVotes(sliceRequest: SusuSliceRequest): Slice<Community> {
        return withContext(Dispatchers.IO){
            communityRepository.findAllByIsActiveAndTypeOrderByCreatedAtDesc(true, CommunityType.VOTE, sliceRequest.toDefault())
        }
    }
}