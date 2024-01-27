package com.oksusu.susu.report.model.request

import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportCreateRequest(
    val metadataId: Long,
    /** 신고 대상 */
    val targetId: Long,
    /** 신고 대상 유형 */
    val targetType: ReportTargetType,
    /** 신고 상세 설명 */
    val description: String? = null,
)
