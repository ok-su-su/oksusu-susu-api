package com.oksusu.susu.report.model.request

import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportCreateRequest(
    /** report metadata id */
    val metadataId: Long,
<<<<<<< HEAD
    /** 신고 대상 id */
    val targetId: Long,
    /** 신고 대상 USER or POST */
    val targetType: ReportTargetType,
    /** 신고 이유 */
=======
    /** 신고 대상 */
    val targetId: Long,
    /** 신고 대상 유형 */
    val targetType: ReportTargetType,
    /** 신고 상세 설명 */
>>>>>>> develop
    val description: String? = null,
)
