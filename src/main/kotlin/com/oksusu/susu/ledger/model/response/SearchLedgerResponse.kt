package com.oksusu.susu.ledger.model.response

import com.oksusu.susu.ledger.domain.Ledger
import java.time.LocalDateTime

data class SearchLedgerResponse(
    val id: Long,
    val uid: Long,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(ledger: Ledger): SearchLedgerResponse {
            return SearchLedgerResponse(
                id = ledger.id,
                uid = ledger.uid,
                title = ledger.title,
                description = ledger.description,
                createdAt = ledger.createdAt
            )
        }
    }
}
