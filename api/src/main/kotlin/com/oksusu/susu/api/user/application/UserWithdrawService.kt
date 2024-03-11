package com.oksusu.susu.api.user.application

import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.user.domain.UserWithdraw
import com.oksusu.susu.domain.user.infrastructure.UserWithdrawRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserWithdrawService(
    private val userWithdrawRepository: UserWithdrawRepository,
) {
    @Transactional
    fun saveSync(userWithdraw: UserWithdraw): UserWithdraw {
        return userWithdrawRepository.save(userWithdraw)
    }

    suspend fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long {
        return withMDCContext(Dispatchers.IO) { userWithdrawRepository.countByCreatedAtBetween(startAt, endAt) }
    }
}
