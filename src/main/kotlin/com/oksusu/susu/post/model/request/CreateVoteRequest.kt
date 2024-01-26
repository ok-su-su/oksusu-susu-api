package com.oksusu.susu.post.model.request

import com.oksusu.susu.post.model.VoteOptionWithoutIdModel

data class CreateVoteRequest(
    /** 투표 내용 */
    val content: String,
    /** 투표 옵션 */
    val options: List<VoteOptionWithoutIdModel>,
    /** 투표 카테고리 id */
    val postCategoryId: Long,
)
