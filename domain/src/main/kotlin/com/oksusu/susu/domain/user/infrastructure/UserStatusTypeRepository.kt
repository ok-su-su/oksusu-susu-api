package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserStatusType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface UserStatusTypeRepository : JpaRepository<UserStatusType, Long> {
    fun findAllByIsActive(isActive: Boolean): List<UserStatusType>
}
