package com.oksusu.susu.friend.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.friend.infrastructure.model.CountPerRelationshipIdModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun countPerRelationshipId(): List<CountPerRelationshipIdModel> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            friendRelationshipRepository.countPerRelationshipId()
        }
    }

    suspend fun countPerRelationshipIdByUid(uid: Long): List<CountPerRelationshipIdModel> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            friendRelationshipRepository.countPerRelationshipIdByUid(uid)
        }
    }

    suspend fun findAllByFriendIds(friendIds: List<Long>): List<FriendRelationship> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            friendRelationshipRepository.findAllByFriendIdIn(
                friendIds
            )
        }
    }

    suspend fun findByFriendIdOrThrow(friendId: Long): FriendRelationship {
        return findByFriendIdOrNull(friendId) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_RELATIONSHIP_ERROR)
    }

    suspend fun findByFriendIdOrNull(friendId: Long): FriendRelationship? {
        return withContext(Dispatchers.IO.withMDCContext()) { friendRelationshipRepository.findByFriendId(friendId) }
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
