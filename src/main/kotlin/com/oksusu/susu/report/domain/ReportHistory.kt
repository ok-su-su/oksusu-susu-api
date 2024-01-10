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

@Entity
@Table(name = "report_history")
class ReportHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: ReportTargetType,

    @Column(name = "metadata_id")
    val metadataId: Long,

    val description: String? = null,
) : BaseEntity()
