package com.oksusu.susu.ledger.model.response

import com.oksusu.susu.ledger.domain.Ledger

data class CreateLedgerResponse(
    val id: Long,
    val uid: Long,
    val title: String,
    val description: String?,
) {
    companion object {
        fun from(ledger: Ledger): CreateLedgerResponse {
            return CreateLedgerResponse(
                id = ledger.id,
                uid = ledger.uid,
                title = ledger.title,
                description = ledger.description
            )
        }
    }
}
