package com.oksusu.susu.post.model.request

data class UpdateVoteRequest(
    val postCategoryId: Long,
    val content: String,
)
