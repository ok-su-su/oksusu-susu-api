package com.oksusu.susu.ledger.model.request

import java.time.LocalDateTime

data class SearchLedgerRequest(
    val title: String?,
    val categoryIds: Set<Long>?,
    val fromStartAt: LocalDateTime?,
    val toEndAt: LocalDateTime?,
)
