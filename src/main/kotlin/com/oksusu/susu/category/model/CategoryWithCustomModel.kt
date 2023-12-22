package com.oksusu.susu.category.model

data class CategoryWithCustomModel(
    val id: Long,
    val seq: Long,
    val category: String,
    val customCategory: String? = null,
) {
    companion object {
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
