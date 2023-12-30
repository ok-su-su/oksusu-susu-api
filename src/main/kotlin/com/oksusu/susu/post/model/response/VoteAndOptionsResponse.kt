package com.oksusu.susu.post.model.response

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel

class VoteAndOptionsResponse(
    val id: Long,
    val uid: Long,
    val category: String,
    val content: String,
    val options: List<VoteOptionModel>,
) {
    companion object {
        fun of(vote: Post, options: List<VoteOptionModel>, postCategoryModel: PostCategoryModel): VoteAndOptionsResponse {
            return VoteAndOptionsResponse(
                id = vote.id,
                uid = vote.uid,
                category = postCategoryModel.name,
                content = vote.content,
                options = options.filter { it.postId == vote.id }
            )
        }
    }
}
