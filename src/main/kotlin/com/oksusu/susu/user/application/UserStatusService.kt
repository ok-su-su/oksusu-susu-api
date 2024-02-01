package com.oksusu.susu.user.application

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
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
    private val txTemplates: TransactionTemplates,
    private val statusService: StatusService,
) {
    @Transactional
    fun saveSync(userStatus: UserStatus): UserStatus {
        return userStatusRepository.save(userStatus)
    }

    suspend fun findByUid(uid: Long): UserStatus {
        return withContext(Dispatchers.IO) {
            userStatusRepository.findByUid(uid)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_STATUS_ERROR)
    }

    suspend fun withdraw(uid: Long) {
        val userStatus = findByUid(uid)

        txTemplates.writer.coExecuteOrNull {
            userStatus.apply {
                accountStatusId = statusService.getDeletedStatusId()
            }.run { saveSync(this) }
        }
    }
}
