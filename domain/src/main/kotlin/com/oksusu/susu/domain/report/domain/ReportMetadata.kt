package com.oksusu.susu.domain.report.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 신고 메타데이터 */
@Entity
@Table(name = "report_metadata")
class ReportMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 노출 순서 */
    val seq: Int,

    /** 신고 메타데이터 */
    val metadata: String,

    /** 신고 대상 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    /** 활성화 상태 1: true, 0: false */
    @Column(name = "is_active")
    val isActive: Boolean,
) : BaseEntity()
