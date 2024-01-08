package com.oksusu.susu.post.model.request

import com.oksusu.susu.post.model.VoteOptionModel

data class CreateVoteRequest(
    val content: String,
    val options: List<VoteOptionModel>,
    val postCategoryId: Long,
)
