package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.VoteSummary
import java.time.LocalDateTime

class VoteCountModel(
    val id: Long,
    val uid: Long,
    val category: String,
    val content: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val count: Int,
) {
    companion object {
        fun of(post: Post, voteSummary: VoteSummary, postCategoryModel: PostCategoryModel): VoteCountModel {
            return VoteCountModel(
                id = post.id,
                uid = post.uid,
                category = postCategoryModel.name,
                content = post.content,
                createdAt = post.createdAt,
                modifiedAt = post.modifiedAt,
                count = voteSummary.count
            )
        }
    }
}
