package com.oksusu.susu.ledger.model

import com.oksusu.susu.ledger.domain.Ledger
import java.time.LocalDateTime

data class LedgerModel(
    val id: Long,
    val title: String,
    val description: String?,
    val startAt: LocalDateTime,
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
