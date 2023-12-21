package com.oksusu.susu.friend.infrastructure

import com.oksusu.susu.friend.domain.FriendRelationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendRelationshipRepository : JpaRepository<FriendRelationship, Long>
