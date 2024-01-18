package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel

data class CreateAndUpdateVoteResponse(
    /** 투표 id */
    val id: Long,
    /** 카테고리 명 */
    val category: String,
    /** 내용 */
    val content: String,
    /** 수정 여부 */
    val isModified: Boolean,
    /** 투표 옵션 */
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
