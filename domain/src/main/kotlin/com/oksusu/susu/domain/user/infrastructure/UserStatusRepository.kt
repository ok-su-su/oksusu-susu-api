package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface UserStatusRepository : JpaRepository<UserStatus, Long>, UserStatusQRepository {
    fun findAllByUidIn(freeUid: Set<Long>): List<UserStatus>
}
