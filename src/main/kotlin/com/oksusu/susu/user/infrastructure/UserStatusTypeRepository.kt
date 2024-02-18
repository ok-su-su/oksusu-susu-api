package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.UserStatusType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserStatusTypeRepository : JpaRepository<UserStatusType, Long> {
    @Transactional(readOnly = true)
    fun findAllByIsActive(isActive: Boolean): List<UserStatusType>
}
