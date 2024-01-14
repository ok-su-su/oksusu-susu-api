package com.oksusu.susu.report.infrastructure

import com.oksusu.susu.report.domain.ReportResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportResultRepository : JpaRepository<ReportResult, Long>
