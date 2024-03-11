package com.oksusu.susu.api.envelope.infrastructure.model

import java.time.LocalDateTime

data class SearchLedgerSpec(
    val uid: Long,
    val title: String?,
    val categoryIds: Set<Long>?,
    val fromStartAt: LocalDateTime?,
    val toStartAt: LocalDateTime?,
)
