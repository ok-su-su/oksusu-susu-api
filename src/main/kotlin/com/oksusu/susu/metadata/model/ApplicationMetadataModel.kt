package com.oksusu.susu.metadata.model

import java.time.LocalDateTime

/** 어플리케이션 설정 정보 */
class ApplicationMetadataModel(
    val id: Long,
    /** 최신 어플리케이션 버전 */
    val applicationVersion: String,
    /** 강제 업데이트 날짜 */
    val forcedUpdateDate: LocalDateTime,
)
