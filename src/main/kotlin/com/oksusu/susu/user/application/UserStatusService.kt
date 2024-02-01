package com.oksusu.susu.user.application

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.user.domain.UserStatus
import com.oksusu.susu.user.infrastructure.UserStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
