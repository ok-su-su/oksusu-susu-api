package com.oksusu.susu.post.model.request

import com.oksusu.susu.post.model.VoteOptionWithoutIdModel

data class CreateVoteRequest(
    /** 투표 내용 */
    val content: String,
    /** 투표 옵션 */
    val options: List<VoteOptionWithoutIdModel>,
    /** 보드 id */
    val boardId: Long,
)
