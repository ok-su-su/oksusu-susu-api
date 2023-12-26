package com.oksusu.susu.community.model.response

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.domain.vo.VoteSummary

class VoteWithCountResponse(
    val id: Long,
    val category: CommunityCategory,
    val content: String,
    val count: Int,
){
    companion object{
        fun of(community: Community, summary: VoteSummary): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = community.id,
                category = community.category,
                content = community.content,
                count = summary.count,
            )
        }
    }
}
