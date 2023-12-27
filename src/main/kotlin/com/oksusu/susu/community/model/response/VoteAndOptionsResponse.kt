package com.oksusu.susu.community.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.model.VoteOptionModel

class VoteAndOptionsResponse(
    val id: Long,
    val uid: Long,
    val category: String,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(vote: Community, options: List<VoteOptionModel>, category: CategoryModel): VoteAndOptionsResponse {
            return VoteAndOptionsResponse(
                id = vote.id,
                uid = vote.uid,
                category = category.name,
                content = vote.content,
                options = options.filter { it.communityId == vote.id }
            )
        }
    }
}
