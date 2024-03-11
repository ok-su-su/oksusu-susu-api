package com.oksusu.susu.api.envelope.model.request

import java.time.LocalDateTime

/** 검색 조건 */
data class SearchLedgerRequest(
    /** 제목 */
    val title: String?,
    val categoryIds: Set<Long>?,
    /** 시작일 */
    val fromStartAt: LocalDateTime?,
    /** 종료일 */
    val toStartAt: LocalDateTime?,

    // TODO : QA-111, 추후에 toStartAt으로 변경해야함. 현재는 안드 대응을 위한 용도임.
    /** 종료일 */
    val toEndAt: LocalDateTime?,
)
