package com.oksusu.susu.ledger.model.response

import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.ledger.model.LedgerModel

data class LedgerDetailResponse(
    val ledger: LedgerModel,
    val category: CategoryWithCustomModel,
    val totalAmounts: Long,
    val totalCounts: Long,
)
