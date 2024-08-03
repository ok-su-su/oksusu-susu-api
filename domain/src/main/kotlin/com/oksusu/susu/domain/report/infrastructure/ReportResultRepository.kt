package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.ReportResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Repository
interface ReportResultRepository : JpaRepository<ReportResult, Long>, ReportResultQRepository {
    fun findAllByCreatedAtBetween(from: LocalDateTime, to: LocalDateTime): List<ReportResult>
}
