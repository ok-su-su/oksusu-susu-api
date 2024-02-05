package com.oksusu.susu.envelope.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.envelope.domain.Ledger
import com.oksusu.susu.envelope.model.LedgerModel

data class CreateAndUpdateLedgerResponse(
    /** 장부 */
    val ledger: LedgerModel,
    /** 카테고리 */
    val category: CategoryWithCustomModel,
) {
    companion object {
        fun of(
            ledger: Ledger,
            category: CategoryModel,
            customCategory: String?,
        ): CreateAndUpdateLedgerResponse {
            return CreateAndUpdateLedgerResponse(
                ledger = LedgerModel.from(ledger),
                category = CategoryWithCustomModel.of(category, customCategory)
            )
        }
    }
}
