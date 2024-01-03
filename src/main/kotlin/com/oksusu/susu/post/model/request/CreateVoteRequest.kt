package com.oksusu.susu.post.model.request

import com.oksusu.susu.post.model.VoteOptionModel

class CreateVoteRequest(
    val content: String,
    val options: List<VoteOptionModel>,
    val postCategoryId: Long,
)
