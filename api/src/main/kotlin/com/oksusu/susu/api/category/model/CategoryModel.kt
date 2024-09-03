package com.oksusu.susu.api.category.model

import com.oksusu.susu.domain.category.domain.Category

data class CategoryModel(
    /** category id */
    val id: Long,
    /** category seq */
    val seq: Long,
    /** category */
    val name: String,
    /** category style */
    val style: String,
    /** 활성화 여부 */
    val isActive: Boolean,
    /** 커스텀 여부 */
    val isCustom: Boolean,
) {
    companion object {
        fun from(category: Category): CategoryModel {
            return CategoryModel(
                id = category.id,
                seq = category.seq,
                name = category.name,
                style = category.style,
                isActive = category.isActive,
                isCustom = category.isCustom,
            )
        }
    }

    fun isMiscCategory(): Boolean {
        return id == 5L
    }
}
