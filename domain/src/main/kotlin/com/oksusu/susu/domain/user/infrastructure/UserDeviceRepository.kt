package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.UserDevice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface UserDeviceRepository : JpaRepository<UserDevice, Long> {
    fun findByUid(uid: Long): UserDevice?
}
