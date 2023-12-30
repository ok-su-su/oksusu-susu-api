package com.oksusu.susu.post.model.response

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel

class CreateVoteResponse(
    val id: Long,
    val category: String,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(post: Post, optionModels: List<VoteOptionModel>, postCategoryModel: PostCategoryModel): CreateVoteResponse {
            return CreateVoteResponse(
                id = post.id,
                category = postCategoryModel.name,
                content = post.content,
                options = optionModels
            )
        }
    }
}
