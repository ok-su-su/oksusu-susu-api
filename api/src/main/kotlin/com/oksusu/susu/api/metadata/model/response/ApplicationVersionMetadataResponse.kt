package com.oksusu.susu.api.metadata.model.response

import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata
import java.time.LocalDateTime

data class ApplicationVersionMetadataResponse(
    /** 최신 어플리케이션 버전 */
    val applicationVersion: String,
    /** 강제 업데이트 날짜 */
    val forcedUpdateDate: LocalDateTime,
    /** 해당 버전의 주요 기능 설명 */
    val description: String?,
) {
    companion object {
        fun from(applicationMetadata: ApplicationMetadata): ApplicationVersionMetadataResponse {
            return ApplicationVersionMetadataResponse(
                applicationVersion = applicationMetadata.applicationVersion,
                forcedUpdateDate = applicationMetadata.forcedUpdateDate,
                description = applicationMetadata.description
            )
        }
    }
}
