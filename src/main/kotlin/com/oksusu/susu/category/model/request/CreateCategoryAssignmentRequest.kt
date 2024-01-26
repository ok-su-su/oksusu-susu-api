package com.oksusu.susu.category.model.request

data class CreateCategoryAssignmentRequest(
    /** category id */
    val id: Long,
    /** custom category */
    val customCategory: String? = null,
)
