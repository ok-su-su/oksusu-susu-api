package com.oksusu.susu.api.envelope.infrastructure.model

import com.oksusu.susu.api.category.domain.CategoryAssignment
import com.oksusu.susu.api.envelope.domain.Ledger
import com.querydsl.core.annotations.QueryProjection

data class SearchLedgerModel @QueryProjection constructor(
    val ledger: Ledger,
    val categoryAssignment: CategoryAssignment,
)
