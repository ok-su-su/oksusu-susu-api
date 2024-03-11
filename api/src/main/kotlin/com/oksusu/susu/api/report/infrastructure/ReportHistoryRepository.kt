package com.oksusu.susu.api.report.infrastructure

import com.oksusu.susu.api.report.domain.ReportHistory
import com.oksusu.susu.api.report.domain.vo.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ReportHistoryRepository : JpaRepository<ReportHistory, Long> {
    @Transactional(readOnly = true)
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: ReportTargetType): Boolean
}
