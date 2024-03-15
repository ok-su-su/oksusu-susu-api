package com.oksusu.susu.domain.log.infrastructure

import com.oksusu.susu.domain.log.domain.SystemActionLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface SystemActionLogRepository : JpaRepository<SystemActionLog, Long> {
    @Transactional(readOnly = true)
    fun findAllByCreatedAtBefore(createdAt: LocalDateTime): List<SystemActionLog>

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}
