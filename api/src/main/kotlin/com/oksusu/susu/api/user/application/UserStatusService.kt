package com.oksusu.susu.api.user.application

import com.oksusu.susu.domain.user.domain.UserStatus
import com.oksusu.susu.domain.user.infrastructure.UserStatusRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserStatusService(
    private val userStatusRepository: UserStatusRepository,
) {
    @Transactional
    fun saveSync(userStatus: UserStatus): UserStatus {
        return userStatusRepository.save(userStatus)
    }
}
