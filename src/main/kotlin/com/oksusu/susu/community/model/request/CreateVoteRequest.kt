package com.oksusu.susu.community.model.request

import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteOptionModel

class CreateVoteRequest(
    val categoryId: Long,
    val content: String,
    val options: List<VoteOptionModel>,
)
