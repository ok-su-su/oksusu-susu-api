package com.oksusu.susu.ledger.model.request

import java.time.LocalDateTime

data class CreateLedgerRequest(
    val title: String,
    val description: String?,
    val categoryId: Long,
    val customCategory: String?,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
)
