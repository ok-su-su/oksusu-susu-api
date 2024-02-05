package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.UserStatusHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>
