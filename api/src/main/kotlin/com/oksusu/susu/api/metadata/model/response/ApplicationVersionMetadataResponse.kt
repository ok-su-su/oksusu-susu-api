package com.oksusu.susu.api.metadata.model.response

import java.time.LocalDateTime

data class ApplicationVersionMetadataResponse(
    /** 최신 어플리케이션 버전 */
    val applicationVersion: String,
    /** 강제 업데이트 날짜 */
    val forcedUpdateDate: LocalDateTime,
)
