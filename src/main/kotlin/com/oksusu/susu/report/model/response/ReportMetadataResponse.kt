package com.oksusu.susu.report.model.response

import com.oksusu.susu.report.model.ReportMetadataModel

data class ReportMetadataResponse(
    /** 신고 관련 메타데이터 */
    val metadata: List<ReportMetadataModel>,
)
