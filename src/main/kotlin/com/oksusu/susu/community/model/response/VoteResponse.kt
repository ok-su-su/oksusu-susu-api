package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteOptionModel

class VoteResponse (
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val options: List<VoteOptionModel>,
)