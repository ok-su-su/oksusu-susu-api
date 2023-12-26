package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteOptionModel

class VoteAndOptionsResponse(
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(vote: Community, options: List<VoteOptionModel>): VoteAndOptionsResponse {
            return VoteAndOptionsResponse(
                id = vote.id,
                uid = vote.uid,
                category = vote.category,
                content = vote.content,
                options = options.filter { it.communityId == vote.id }
            )
        }
    }
}
