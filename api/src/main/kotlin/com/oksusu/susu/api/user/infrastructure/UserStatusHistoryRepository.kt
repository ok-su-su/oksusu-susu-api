package com.oksusu.susu.api.user.infrastructure

import com.oksusu.susu.api.user.domain.UserStatusHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>
