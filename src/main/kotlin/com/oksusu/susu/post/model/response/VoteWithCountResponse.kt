package com.oksusu.susu.post.model.response

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.VoteSummary
import com.oksusu.susu.post.model.PostCategoryModel

class VoteWithCountResponse(
    val id: Long,
    val category: String,
    val content: String,
    val count: Int,
) {
    companion object {
        fun of(post: Post, summary: VoteSummary, postCategoryModel: PostCategoryModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = post.id,
                category = postCategoryModel.name,
                content = post.content,
                count = summary.count
            )
        }
    }
}
