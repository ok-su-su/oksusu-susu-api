package com.oksusu.susu.api.report.infrastructure

import com.oksusu.susu.api.report.domain.ReportResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportResultRepository : JpaRepository<ReportResult, Long>
