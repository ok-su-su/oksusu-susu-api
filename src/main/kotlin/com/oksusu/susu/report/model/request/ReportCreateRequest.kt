package com.oksusu.susu.report.model.request

import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportCreateRequest(
    /** report metadata id */
    val metadataId: Long,
    /** 신고 대상 id */
    val targetId: Long,
    /** 신고 대상 USER or POST */
    val targetType: ReportTargetType,
    /** 신고 이유 */
    val description: String? = null,
)
