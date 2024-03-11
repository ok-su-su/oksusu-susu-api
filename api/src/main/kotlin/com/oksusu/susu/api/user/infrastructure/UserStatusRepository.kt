package com.oksusu.susu.api.user.infrastructure

import com.oksusu.susu.api.user.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusRepository : JpaRepository<UserStatus, Long>
