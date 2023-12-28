package com.oksusu.susu.ledger.infrastructure.model

import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.ledger.domain.Ledger
import com.querydsl.core.annotations.QueryProjection

data class LedgerDetailModel @QueryProjection constructor(
    val ledger: Ledger,
    val categoryAssignment: CategoryAssignment,
)
