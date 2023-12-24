package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteOptionModel

class VoteResponse(
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(vote: Community, options: List<VoteOptionModel>): VoteResponse {
            return VoteResponse(
                id = vote.id,
                uid = vote.uid,
                category = vote.category,
                content = vote.content,
                options = options.filter { it.communityId == vote.id }
            )
        }
    }
}