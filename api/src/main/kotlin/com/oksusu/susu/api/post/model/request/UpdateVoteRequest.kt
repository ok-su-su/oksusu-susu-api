package com.oksusu.susu.api.post.model.request

data class UpdateVoteRequest(
    /** 보드 id */
    val boardId: Long,
    /** 투표 내용 */
    val content: String,
)
