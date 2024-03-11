package com.oksusu.susu.api.report.model.response

data class ReportCreateResponse(
    /** 신고 히스토리 id */
    val historyId: Long,
    /** 신고 메타데이터 id */
    val metadataId: Long,
)
