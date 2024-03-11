package com.oksusu.susu.api.envelope.model.response

import com.oksusu.susu.api.category.model.CategoryWithCustomModel
import com.oksusu.susu.api.envelope.model.LedgerModel

data class SearchLedgerResponse(
    /** 장부 */
    val ledger: LedgerModel,
    /** 카테고리 */
    val category: CategoryWithCustomModel,
    /** 금액 총합 */
    val totalAmounts: Long,
    /** 봉투 수 총합 */
    val totalCounts: Long,
)
