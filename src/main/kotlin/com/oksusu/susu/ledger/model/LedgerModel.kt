package com.oksusu.susu.ledger.model

import com.oksusu.susu.ledger.domain.Ledger
import java.time.LocalDateTime

/** 장부 공통 모델 */
data class LedgerModel(
    val id: Long,
    /** 제목 */
    val title: String,
    /** 상세 설명 */
    val description: String?,
    /** 시작일 */
    val startAt: LocalDateTime,
    /** 종료일 */
    val endAt: LocalDateTime,
) {
    companion object {
        fun from(ledger: Ledger): LedgerModel {
            return LedgerModel(
                id = ledger.id,
                title = ledger.title,
                description = ledger.description,
                startAt = ledger.startAt,
                endAt = ledger.endAt
            )
        }
    }
}
