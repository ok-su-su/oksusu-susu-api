package com.oksusu.susu.community.model

import com.oksusu.susu.category.domain.Category
import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.VoteSummary
import com.oksusu.susu.community.domain.vo.CommunityCategory
import java.time.LocalDateTime

class VoteCountModel (
    val id: Long,
    val uid: Long,
    val category: String,
    val content: String,
    val createdAt: LocalDateTime,
    val count: Int,
) {
    companion object {
        fun of(community: Community, voteSummary: VoteSummary, category: CategoryModel): VoteCountModel {
            return VoteCountModel(
                id = community.id,
                uid = community.uid,
                category = category.name,
                content = community.content,
                createdAt = community.createdAt,
                count = voteSummary.count,
            )
        }
    }
}
