package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.VoteOption
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteOptionModel

class CreateVoteResponse(
    val id: Long,
    val category: CommunityCategory,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object{
        fun of(community: Community, optionModels: List<VoteOptionModel>): CreateVoteResponse {
            return CreateVoteResponse(
                id = community.id,
                category = community.category,
                content = community.content,
                options = optionModels
            )
        }
    }
}