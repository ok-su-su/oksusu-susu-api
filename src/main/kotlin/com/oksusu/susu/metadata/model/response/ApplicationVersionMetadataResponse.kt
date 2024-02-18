package com.oksusu.susu.metadata.model.response

import java.time.LocalDateTime

class ApplicationVersionMetadataResponse(
    /** 최신 어플리케이션 버전 */
    var applicationVersion: String,
    /** 강제 업데이트 날짜 */
    var forcedUpdateDate: LocalDateTime,
)
