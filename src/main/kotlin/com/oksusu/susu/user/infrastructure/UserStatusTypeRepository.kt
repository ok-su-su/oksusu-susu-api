package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.UserStatusType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserStatusTypeRepository : JpaRepository<UserStatusType, Long> {
    fun findAllByIsActive(isActive: Boolean): List<UserStatusType>
}
