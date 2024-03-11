package com.oksusu.susu.api.user.application

import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserStatusHistoryService(
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
) {
    @Transactional
    fun saveSync(userStatusHistory: UserStatusHistory): UserStatusHistory {
        return userStatusHistoryRepository.save(userStatusHistory)
    }
}
