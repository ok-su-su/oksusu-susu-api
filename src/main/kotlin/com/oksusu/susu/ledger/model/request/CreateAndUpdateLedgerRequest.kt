package com.oksusu.susu.ledger.model.request

import java.time.LocalDateTime

data class CreateAndUpdateLedgerRequest(
    /** 제목 */
    val title: String,
    /** 상세 설명 */
    val description: String?,
    val categoryId: Long,
    /** 커스텀 카테고리인 경우 */
    val customCategory: String?,
    /** 시작일 */
    val startAt: LocalDateTime,
    /** 종료일 */
    val endAt: LocalDateTime,
)
