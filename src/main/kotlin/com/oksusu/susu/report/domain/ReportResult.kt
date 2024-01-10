package com.oksusu.susu.report.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.report.domain.vo.ReportResultStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "report_result")
class ReportResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: ReportResultStatus,
) : BaseEntity()
