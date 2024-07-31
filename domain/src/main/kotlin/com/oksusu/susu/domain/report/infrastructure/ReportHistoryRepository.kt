package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface ReportHistoryRepository : JpaRepository<ReportHistory, Long>, ReportHistoryQRepository {
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: ReportTargetType): Boolean

    fun findAllByCreatedAtAfter(createdAt: LocalDateTime): List<ReportHistory>

    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}
