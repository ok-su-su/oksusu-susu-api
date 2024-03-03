package com.oksusu.susu.user.application

import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.user.domain.UserWithdraw
import com.oksusu.susu.user.infrastructure.UserWithdrawRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO.withMDCContext()) {
            userWithdrawRepository.countByCreatedAtBetween(
                startAt,
                endAt
            )
        }
    }
}
