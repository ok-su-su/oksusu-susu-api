package com.oksusu.susu.domain.friend.infrastructure

import com.oksusu.susu.domain.friend.domain.Friend
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface FriendRepository : JpaRepository<Friend, Long>, FriendQRepository {
    fun findByIdAndUid(id: Long, uid: Long): Friend?

    fun existsByUidAndPhoneNumber(uid: Long, phoneNumber: String): Boolean

    fun findAllByIdIn(ids: List<Long>): List<Friend>

    fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Friend>

    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long

    fun findAllByUidIn(uid: List<Long>): List<Friend>
}
