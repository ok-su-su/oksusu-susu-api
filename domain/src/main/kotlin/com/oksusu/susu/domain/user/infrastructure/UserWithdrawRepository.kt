package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserWithdraw
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface UserWithdrawRepository : JpaRepository<UserWithdraw, Long> {
    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}
