package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel

data class CreateAndUpdateVoteResponse(
    val id: Long,
    val category: String,
    val content: String,
    val isModified: Boolean,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(
            post: Post,
            optionModels: List<VoteOptionModel>,
            postCategoryModel: PostCategoryModel,
        ): CreateAndUpdateVoteResponse {
            return CreateAndUpdateVoteResponse(
                id = post.id,
                category = postCategoryModel.name,
                content = post.content,
                isModified = !post.createdAt.equalsFromYearToSec(post.modifiedAt),
                options = optionModels
            )
        }
    }
}
