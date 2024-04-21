package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserStatusHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>
