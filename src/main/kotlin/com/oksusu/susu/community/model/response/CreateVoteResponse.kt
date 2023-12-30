package com.oksusu.susu.community.model.response

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.model.VoteOptionModel

class CreateVoteResponse(
    val id: Long,
    val category: String,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(post: Post, optionModels: List<VoteOptionModel>, category: CategoryModel): CreateVoteResponse {
            return CreateVoteResponse(
                id = post.id,
                category = category.name,
                content = post.content,
                options = optionModels
            )
        }
    }
}
