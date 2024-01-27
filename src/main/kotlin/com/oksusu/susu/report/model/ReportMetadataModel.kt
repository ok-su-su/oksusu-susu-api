package com.oksusu.susu.report.model

import com.oksusu.susu.report.domain.ReportMetadata
import com.oksusu.susu.report.domain.vo.ReportTargetType

data class ReportMetadataModel(
    /** 메타데이터 id */
    val id: Long,
    /** 메타데이터 seq */
    val seq: Int,
    /** 메타데이터 seq */
    val targetType: ReportTargetType,
    /** 신고 메타데이터 */
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
