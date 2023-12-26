package com.oksusu.susu.ledger.infrastructure.model

import java.time.LocalDateTime

data class SearchLedgerSpec(
    val uid: Long,
    val categoryId: Long?,
    val fromStartAt: LocalDateTime?,
    val toStartAt: LocalDateTime?,
)
