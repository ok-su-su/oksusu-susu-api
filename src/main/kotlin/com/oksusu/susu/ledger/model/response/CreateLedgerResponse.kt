package com.oksusu.susu.ledger.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.category.model.CategoryWithCustomModel
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.model.LedgerModel

data class CreateLedgerResponse(
    val ledger: LedgerModel,
    val category: CategoryWithCustomModel,
) {
    companion object {
        fun of(
            ledger: Ledger,
            category: CategoryModel,
            customCategory: String?,
        ): CreateLedgerResponse {
            return CreateLedgerResponse(
                ledger = LedgerModel.from(ledger),
                category = CategoryWithCustomModel.of(category, customCategory)
            )
        }
    }
}
