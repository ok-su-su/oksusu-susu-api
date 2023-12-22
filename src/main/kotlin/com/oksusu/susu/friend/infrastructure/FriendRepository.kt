package com.oksusu.susu.friend.infrastructure

import com.oksusu.susu.friend.domain.Friend
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendRepository : JpaRepository<Friend, Long>
