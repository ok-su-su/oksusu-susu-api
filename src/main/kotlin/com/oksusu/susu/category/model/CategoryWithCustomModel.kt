package com.oksusu.susu.category.model

import com.oksusu.susu.category.domain.CategoryAssignment

data class CategoryWithCustomModel(
    val id: Long,
    val seq: Long,
    val category: String,
    val customCategory: String? = null,
) {
    companion object {
        fun of(category: CategoryModel, categoryAssignment: CategoryAssignment): CategoryWithCustomModel {
            return CategoryWithCustomModel(
                id = category.id,
                seq = category.seq,
                category = category.name,
                customCategory = categoryAssignment.customCategory
            )
        }

        fun of(category: CategoryModel, customCategory: String?): CategoryWithCustomModel {
            return CategoryWithCustomModel(
                id = category.id,
                seq = category.seq,
                category = category.name,
                customCategory = customCategory
            )
        }
    }
}
