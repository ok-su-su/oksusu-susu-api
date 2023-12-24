package com.oksusu.susu.community.model

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.VoteSummary
import com.oksusu.susu.community.domain.vo.CommunityCategory

class VoteCountModel (
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val count: Int,
) {
    companion object {
        fun of(community: Community, voteSummary: VoteSummary): VoteCountModel {
            return VoteCountModel(
                id = community.id,
                uid = community.uid,
                category = community.category,
                content = community.content,
                count = voteSummary.count,
            )
        }
    }
}