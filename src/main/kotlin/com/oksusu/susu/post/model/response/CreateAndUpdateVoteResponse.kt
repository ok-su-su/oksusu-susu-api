package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel
import java.time.LocalDateTime

data class CreateAndUpdateVoteResponse(
    /** 투표 id */
    val id: Long,
    /** 투표 생성자 id */
    val uid: Long,
    /** 카테고리 명 */
    val category: String,
    /** 내용 */
    val content: String,
    /** 수정 여부 */
    val isModified: Boolean,
    /** 투표 옵션 */
    val options: List<VoteOptionModel>,
    /** 투표 생성일 */
    val createdAt: LocalDateTime
) {
    companion object {
        fun of(
            uid: Long,
            post: Post,
            optionModels: List<VoteOptionModel>,
            postCategoryModel: PostCategoryModel,
        ): CreateAndUpdateVoteResponse {
            return CreateAndUpdateVoteResponse(
                id = post.id,
                uid = uid,
                category = postCategoryModel.name,
                content = post.content,
                isModified = !post.createdAt.equalsFromYearToSec(post.modifiedAt),
                options = optionModels,
                createdAt = post.createdAt
            )
        }
    }
}
