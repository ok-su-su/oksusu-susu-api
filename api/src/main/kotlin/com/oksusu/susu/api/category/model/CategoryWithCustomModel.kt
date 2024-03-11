package com.oksusu.susu.api.category.model

import com.oksusu.susu.api.category.domain.CategoryAssignment

/** 커스텀 카테고리 정보를 포함 모델 */
data class CategoryWithCustomModel(
    /** 카테고리 id */
    val id: Long,
    /** 카테고리 순서 */
    val seq: Long,
    /** 카테고리 */
    val category: String,
    /** 커스텀 카테고리, 기타 항목일 경우에만 제공 */
    val customCategory: String? = null,
    /** 카테고리 스타일 */
    val style: String,
) {
    companion object {
        fun of(category: CategoryModel, categoryAssignment: CategoryAssignment): CategoryWithCustomModel {
            return CategoryWithCustomModel(
                id = category.id,
                seq = category.seq,
                category = category.name,
                customCategory = categoryAssignment.customCategory,
                style = category.style
            )
        }

        fun of(category: CategoryModel, customCategory: String?): CategoryWithCustomModel {
            return CategoryWithCustomModel(
                id = category.id,
                seq = category.seq,
                category = category.name,
                customCategory = customCategory,
                style = category.style
            )
        }
    }
}
