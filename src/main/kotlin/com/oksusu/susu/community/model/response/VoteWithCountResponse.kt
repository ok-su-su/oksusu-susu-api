package com.oksusu.susu.community.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.VoteSummary

class VoteWithCountResponse(
    val id: Long,
    val category: String,
    val content: String,
    val count: Int,
) {
    companion object {
        fun of(community: Community, summary: VoteSummary, category: CategoryModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = community.id,
                category = category.name,
                content = community.content,
                count = summary.count
            )
        }
    }
}
