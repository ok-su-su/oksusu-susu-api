package com.oksusu.susu.report.model

import com.oksusu.susu.report.domain.ReportMetadata
import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportMetadataModel(
    val id: Long,
    val seq: Int,
    val targetType: ReportTargetType,
    val metadata: String,
) {
    companion object {
        fun from(reportMetadata: ReportMetadata): ReportMetadataModel {
            return ReportMetadataModel(
                id = reportMetadata.id,
                seq = reportMetadata.seq,
                targetType = reportMetadata.targetType,
                metadata = reportMetadata.metadata
            )
        }
    }
}
