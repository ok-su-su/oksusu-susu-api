package com.oksusu.susu.category.model.request

data class CreateCategoryAssignmentRequest(
    val id: Long,
    val customCategory: String? = null,
)
