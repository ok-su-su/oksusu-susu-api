package com.oksusu.susu.community.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.domain.vo.VoteSummary

class VoteWithCountResponse(
    val id: Long,
    val category: String,
    val content: String,
    val count: Int,
) {
    companion object {
        fun of(post: Post, summary: VoteSummary, category: CategoryModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = post.id,
                category = category.name,
                content = post.content,
                count = summary.count
            )
        }
    }
}
