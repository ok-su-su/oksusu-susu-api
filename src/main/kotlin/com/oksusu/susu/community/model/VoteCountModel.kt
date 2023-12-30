package com.oksusu.susu.community.model

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.domain.vo.VoteSummary
import java.time.LocalDateTime

class VoteCountModel(
    val id: Long,
    val uid: Long,
    val category: String,
    val content: String,
    val createdAt: LocalDateTime,
    val count: Int,
) {
    companion object {
        fun of(post: Post, voteSummary: VoteSummary, category: CategoryModel): VoteCountModel {
            return VoteCountModel(
                id = post.id,
                uid = post.uid,
                category = category.name,
                content = post.content,
                createdAt = post.createdAt,
                count = voteSummary.count
            )
        }
    }
}
