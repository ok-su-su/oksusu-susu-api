package com.oksusu.susu.community.model

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.VoteSummary
import com.oksusu.susu.community.domain.vo.CommunityCategory
import java.time.LocalDateTime

class VoteCountModel (
    val id: Long,
    val uid: Long,
    val category: CommunityCategory,
    val content: String,
    val createdAt: LocalDateTime,
    val count: Int,
) {
    companion object {
        fun of(community: Community, voteSummary: VoteSummary): VoteCountModel {
            return VoteCountModel(
                id = community.id,
                uid = community.uid,
                category = community.category,
                content = community.content,
                createdAt = community.createdAt,
                count = voteSummary.count,
            )
        }
    }
}