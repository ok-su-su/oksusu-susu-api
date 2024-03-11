package com.oksusu.susu.api.friend.application

import com.oksusu.susu.api.exception.ErrorCode
import com.oksusu.susu.api.exception.NotFoundException
import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.friend.domain.Friend
import com.oksusu.susu.api.friend.infrastructure.FriendRepository
import com.oksusu.susu.api.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.api.friend.infrastructure.model.SearchFriendSpec
import kotlinx.coroutines.Dispatchers
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FriendService(
    private val friendRepository: FriendRepository,
) {
    suspend fun search(
        spec: SearchFriendSpec,
        pageable: Pageable,
    ): Page<FriendAndFriendRelationshipModel> {
        return withMDCContext(Dispatchers.IO) { friendRepository.search(spec, pageable) }
    }

    suspend fun findByIdAndUidOrThrow(id: Long, uid: Long): Friend {
        return findByIdAndUidOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_ERROR)
    }

    suspend fun findByIdAndUidOrNull(id: Long, uid: Long): Friend? {
        return withMDCContext(Dispatchers.IO) { friendRepository.findByIdAndUid(id, uid) }
    }

    @Transactional
    fun saveSync(friend: Friend): Friend {
        return friendRepository.save(friend)
    }

    suspend fun existsByUidAndPhoneNumber(uid: Long, phoneNumber: String): Boolean {
        return withMDCContext(Dispatchers.IO) { friendRepository.existsByUidAndPhoneNumber(uid, phoneNumber) }
    }

    suspend fun findByIdOrThrow(id: Long): Friend {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_FRIEND_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Friend? {
        return withMDCContext(Dispatchers.IO) { friendRepository.findByIdOrNull(id) }
    }

    suspend fun findAllByIdIn(ids: List<Long>): List<Friend> {
        return withMDCContext(Dispatchers.IO) { friendRepository.findAllByIdIn(ids) }
    }

    suspend fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Friend> {
        return withMDCContext(Dispatchers.IO) { friendRepository.findAllByUidAndIdIn(uid, ids) }
    }

    @Transactional
    fun deleteSync(ids: List<Long>) {
        friendRepository.deleteAllByIdInBatch(ids)
    }

    @Transactional
    fun deleteSync(id: Long) {
        friendRepository.deleteById(id)
    }

    suspend fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long {
        return withMDCContext(Dispatchers.IO) { friendRepository.countByCreatedAtBetween(startAt, endAt) }
    }
}
