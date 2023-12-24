package com.oksusu.susu.community.application

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.infrastructure.repository.CommunityRepository
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.repository.findByIdOrNull
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

    suspend fun findAllByIsActiveAndTypeOrderByCreatedAtDes(
        isActive: Boolean,
        type: CommunityType,
        pageable: Pageable
    ): Slice<Community> {
        return withContext(Dispatchers.IO) {
            communityRepository.findAllByIsActiveAndTypeOrderByCreatedAtDesc(
                isActive, type, pageable
            )
        }
    }

    suspend fun findByIdOrThrow(id: Long): Community {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_COMMUNITY_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Community? {
        return withContext(Dispatchers.IO) {
            communityRepository.findByIdOrNull(id)
        }
    }

    suspend fun findByIdAndIsActiveAndTypeOrThrow(id: Long, isActive: Boolean, type: CommunityType): Community {
        return withContext(Dispatchers.IO) {
            communityRepository.findByIdAndIsActiveAndType(id, true, CommunityType.VOTE)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_COMMUNITY_ERROR)
    }
}