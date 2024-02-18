package com.oksusu.susu.post.model.request

import jakarta.validation.constraints.Positive

data class UpdateVoteRequest(
    /** 보드 id */
    @field:Positive
    val boardId: Long,
    /** 투표 내용 */
    val content: String,
)
