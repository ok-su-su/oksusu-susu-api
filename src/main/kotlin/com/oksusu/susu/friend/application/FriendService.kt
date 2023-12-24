package com.oksusu.susu.friend.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.infrastructure.FriendRepository
import com.oksusu.susu.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.friend.infrastructure.model.SearchFriendRequestModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendService(
    private val friendRepository: FriendRepository,
    private val relationshipService: RelationshipService,
) {
    suspend fun search(
        searchRequest: SearchFriendRequestModel,
        pageable: Pageable,
    ): Page<FriendAndFriendRelationshipModel> {
        return withContext(Dispatchers.IO) { friendRepository.search(searchRequest, pageable) }
    }

    suspend fun findByIdAndUidOrThrow(id: Long, uid: Long): Friend {
        return findByIdAndUidOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_ERROR)
    }

    suspend fun findByIdAndUidOrNull(id: Long, uid: Long): Friend? {
        return withContext(Dispatchers.IO) { friendRepository.findByIdAndUid(id, uid) }
    }

    @Transactional
    fun saveSync(friend: Friend): Friend {
        return friendRepository.save(friend)
    }

    suspend fun existsByPhoneNumber(uid: Long, phoneNumber: String): Boolean {
        return withContext(Dispatchers.IO) { friendRepository.existsByUidAndPhoneNumber(uid, phoneNumber) }
    }
}
