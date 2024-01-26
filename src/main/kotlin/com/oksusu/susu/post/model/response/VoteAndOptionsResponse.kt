package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.PostCategoryModel
import com.oksusu.susu.post.model.VoteOptionModel
import java.time.LocalDateTime

class VoteAndOptionsResponse(
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
            vote: Post,
            options: List<VoteOptionModel>,
            postCategoryModel: PostCategoryModel,
        ): VoteAndOptionsResponse {
            return VoteAndOptionsResponse(
                id = vote.id,
                uid = vote.uid,
                category = postCategoryModel.name,
                content = vote.content,
                isModified = !vote.createdAt.equalsFromYearToSec(vote.modifiedAt),
                options = options.filter { it.postId == vote.id },
                createdAt = vote.createdAt,
            )
        }
    }
}
