package com.oksusu.susu.post.model.request

data class UpdateVoteRequest(
    /** 투표 카테고리 id */
    val postCategoryId: Long,
    /** 투표 내용 */
    val content: String,
)
