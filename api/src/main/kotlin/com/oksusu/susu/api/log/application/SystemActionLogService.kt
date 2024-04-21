package com.oksusu.susu.api.log.application

import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.log.domain.SystemActionLog
import com.oksusu.susu.domain.log.infrastructure.SystemActionLogRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SystemActionLogService(
    private val systemActionLogRepository: SystemActionLogRepository,
) {
    @Transactional
    fun record(systemActionLog: SystemActionLog) {
        systemActionLogRepository.save(systemActionLog)
    }

    fun findAllByCreatedAtBefore(targetDate: LocalDateTime): List<SystemActionLog> {
        return systemActionLogRepository.findAllByCreatedAtBefore(targetDate)
    }

    @Transactional
    fun deleteAllBy(systemActionLogs: List<SystemActionLog>) {
        systemActionLogRepository.deleteAllInBatch(systemActionLogs)
    }

    suspend fun countByCreatedAtBetween(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ): Long {
        return withMDCContext(Dispatchers.IO) {
            systemActionLogRepository.countByCreatedAtBetween(
                startAt,
                endAt
            )
        }
    }
}
