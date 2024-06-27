package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface ReportHistoryRepository : JpaRepository<ReportHistory, Long> {
    @Transactional(readOnly = true)
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: ReportTargetType): Boolean

    @Transactional(readOnly = true)
    fun findAllByCreatedAtAfter(createdAt: LocalDateTime): List<ReportHistory>
}
