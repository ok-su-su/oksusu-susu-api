package com.oksusu.susu.report.infrastructure

import com.oksusu.susu.report.domain.ReportMetadata
import com.oksusu.susu.report.domain.vo.ReportTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ReportMetadataRepository : JpaRepository<ReportMetadata, Long> {
    @Transactional(readOnly = true)
    fun findAllByTargetTypeAndIsActive(targetType: ReportTargetType, isActive: Boolean): List<ReportMetadata>

    @Transactional(readOnly = true)
    fun findAllByIsActive(isActive: Boolean): List<ReportMetadata>
}
