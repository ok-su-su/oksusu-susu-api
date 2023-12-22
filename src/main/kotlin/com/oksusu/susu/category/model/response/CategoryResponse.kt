package com.oksusu.susu.category.model.response

import com.oksusu.susu.category.domain.Category

data class CategoriesResponse(
    val categories: List<CategoryResponse>,
)

data class CategoryResponse(
    val id: Long,
    val seq: Long,
    val name: String,
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id,
                seq = category.seq,
                name = category.name
            )
        }
    }
}
