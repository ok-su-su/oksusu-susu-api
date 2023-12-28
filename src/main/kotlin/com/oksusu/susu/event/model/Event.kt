package com.oksusu.susu.event.model

import com.oksusu.susu.ledger.domain.Ledger

sealed interface Event

data class DeleteLedgerEvent(
    val ledger: Ledger,
) : Event
