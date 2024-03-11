package com.oksusu.susu.api.category.model.request

data class CreateCategoryAssignmentRequest(
    /** category id */
    val id: Long,
    /** custom category */
    val customCategory: String? = null,
)
