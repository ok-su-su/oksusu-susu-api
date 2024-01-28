package com.oksusu.susu.log.application

import com.oksusu.susu.log.domain.SystemActionLog
import com.oksusu.susu.log.infrastructure.SystemActionLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SystemActionLogService(
    private val systemActionLogRepository: SystemActionLogRepository,
) {
    @Transactional
    fun record(systemActionLog: SystemActionLog) {
        systemActionLogRepository.save(systemActionLog)
    }
}
