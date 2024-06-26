package com.oksusu.susu.api.friend.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.model.CountPerRelationshipIdModel
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendRelationshipService(
    private val friendRelationshipRepository: FriendRelationshipRepository,
) {
    @Transactional
    fun saveSync(friendRelationship: FriendRelationship): FriendRelationship {
        return friendRelationshipRepository.save(friendRelationship)
    }

    suspend fun countPerRelationshipIdByUid(uid: Long): List<CountPerRelationshipIdModel> {
        return withMDCContext(Dispatchers.IO) { friendRelationshipRepository.countPerRelationshipIdByUid(uid) }
    }

    suspend fun findAllByFriendIds(friendIds: List<Long>): List<FriendRelationship> {
        return withMDCContext(Dispatchers.IO) { friendRelationshipRepository.findAllByFriendIdIn(friendIds) }
    }

    suspend fun findByFriendIdOrThrow(friendId: Long): FriendRelationship {
        return findByFriendIdOrNull(friendId) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_RELATIONSHIP_ERROR)
    }

    suspend fun findByFriendIdOrNull(friendId: Long): FriendRelationship? {
        return withMDCContext(Dispatchers.IO) { friendRelationshipRepository.findByFriendId(friendId) }
    }

    @Transactional
    fun deleteByFriendIdInSync(friendIds: List<Long>) {
        friendRelationshipRepository.deleteByFriendIdIn(friendIds)
    }

    @Transactional
    fun deleteByFriendIdSync(friendId: Long) {
        friendRelationshipRepository.deleteByFriendId(friendId)
    }
}
