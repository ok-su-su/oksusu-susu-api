package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.VoteCountModel
import com.oksusu.susu.community.model.VoteOptionCountModel
import com.oksusu.susu.community.model.VoteOptionModel

class VoteCountResponse(
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val count: Int,
    val options: List<VoteOptionCountModel>,
) {
    companion object {
        fun of(vote: VoteCountModel, options: List<VoteOptionCountModel>): VoteCountResponse {
            return VoteCountResponse(
                id = vote.id,
                uid = vote.uid,
                category = vote.category,
                content = vote.content,
                count = vote.count,
                options = options,
            )
        }
    }
}