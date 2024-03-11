package com.oksusu.susu.domain.envelope.infrastructure.model

import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.querydsl.core.annotations.QueryProjection

data class SearchLedgerModel @QueryProjection constructor(
    val ledger: Ledger,
    val categoryAssignment: CategoryAssignment,
)
