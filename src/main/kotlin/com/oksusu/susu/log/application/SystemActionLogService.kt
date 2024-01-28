package com.oksusu.susu.log.application

import com.oksusu.susu.log.domain.SystemActionLog
import com.oksusu.susu.log.infrastructure.SystemActionLogRepository
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
}
