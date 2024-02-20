package com.oksusu.susu.post.model.request

import com.oksusu.susu.post.model.VoteOptionWithoutIdModel
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

data class CreateVoteRequest(
    /** 투표 내용 */
    val content: String,
    /** 투표 옵션 */
    val options: List<VoteOptionWithoutIdModel>,
    /** 보드 id */
    val boardId: Long,
)
