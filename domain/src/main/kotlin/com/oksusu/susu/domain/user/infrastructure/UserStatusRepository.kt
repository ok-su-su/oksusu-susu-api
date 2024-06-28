package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserStatusRepository : JpaRepository<UserStatus, Long> {
    @Transactional(readOnly = true)
    fun findAllByUidIn(freeUid: List<Long>): List<UserStatus>
}
