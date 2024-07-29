package com.oksusu.susu.domain.friend.infrastructure

import com.oksusu.susu.domain.friend.domain.FriendRelationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface FriendRelationshipRepository : JpaRepository<FriendRelationship, Long>, FriendRelationshipQRepository {
    fun findAllByFriendIdIn(friendIds: List<Long>): List<FriendRelationship>

    fun findByFriendId(friendId: Long): FriendRelationship?

    fun deleteByFriendIdIn(friendIds: List<Long>)

    fun deleteByFriendId(friendId: Long)
}
