package com.oksusu.susu.domain.report.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.report.domain.vo.ReportResultStatus
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 신고 결과 */
@Entity
@Table(name = "report_result")
class ReportResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 신고 대상 id */
    @Column(name = "target_id")
    val targetId: Long,

    /** 신고 대상 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    /** 신고 결과 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: ReportResultStatus,
) : BaseEntity()
