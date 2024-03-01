package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.UserWithdraw
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserWithdrawRepository : JpaRepository<UserWithdraw, Long>