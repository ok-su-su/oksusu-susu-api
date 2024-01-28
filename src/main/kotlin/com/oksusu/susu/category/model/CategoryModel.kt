package com.oksusu.susu.category.model

import com.oksusu.susu.category.domain.Category

data class CategoryModel(
    /** category id */
    val id: Long,
    /** category seq */
    val seq: Long,
    /** category */
    val name: String,
    /** category style */
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

    fun isMiscCategory(): Boolean {
        return id == 5L
    }
}
