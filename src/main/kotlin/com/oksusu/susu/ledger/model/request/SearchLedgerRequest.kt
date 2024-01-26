package com.oksusu.susu.ledger.model.request

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
)
