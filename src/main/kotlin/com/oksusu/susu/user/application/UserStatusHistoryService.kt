package com.oksusu.susu.user.application

import com.oksusu.susu.user.domain.UserStatusHistory
import com.oksusu.susu.user.infrastructure.UserStatusHistoryRepository
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
