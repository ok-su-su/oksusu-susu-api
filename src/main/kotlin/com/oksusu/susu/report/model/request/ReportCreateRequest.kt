package com.oksusu.susu.report.model.request

import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportCreateRequest(
    val metadataId: Long,
    val targetId: Long,
    val targetType: ReportTargetType,
    val description: String? = null,
)
