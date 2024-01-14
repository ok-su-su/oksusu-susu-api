package com.oksusu.susu.category.model

import com.oksusu.susu.category.domain.Category

data class CategoryModel(
    val id: Long,
    val seq: Long,
    val name: String,
    val style: String,
) {
    companion object {
        fun from(category: Category): CategoryModel {
            return CategoryModel(
                id = category.id,
                seq = category.seq,
                name = category.name,
                style = category.style
            )
        }
    }
}
