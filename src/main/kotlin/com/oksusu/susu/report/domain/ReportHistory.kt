package com.oksusu.susu.report.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.report.domain.vo.ReportTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 신고 기록 */
@Entity
@Table(name = "report_history")
class ReportHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 신고를 보낸 사용자 */
    val uid: Long,

    /** 신고 대상 */
    @Column(name = "target_id")
    val targetId: Long,

    /** 신고 대상 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    /** 신고 대상 유형 */
    @Column(name = "metadata_id")
    val metadataId: Long,

    /** 신고 상세 설명 */
    val description: String? = null,
) : BaseEntity()
