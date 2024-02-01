package com.oksusu.susu.user.infrastructure

import com.oksusu.susu.user.domain.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StatusRepository : JpaRepository<Status, Long> {
    fun findAllByIsActive(isActive: Boolean): List<Status>
}
