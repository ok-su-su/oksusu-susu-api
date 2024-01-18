package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.vo.VoteSummary
import com.oksusu.susu.post.model.PostCategoryModel

data class VoteWithCountResponse(
    /** 투표 id */
    val id: Long,
    /** 카테고리 명 */
    val category: String,
    /** 내용 */
    val content: String,
    /** 총 투표 수 */
    val count: Int,
    /** 수정 여부 */
    val isModified: Boolean,
) {
    companion object {
        fun of(post: Post, summary: VoteSummary, postCategoryModel: PostCategoryModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = post.id,
                category = postCategoryModel.name,
                content = post.content,
                count = summary.count,
                isModified = !post.createdAt.equalsFromYearToSec(post.modifiedAt)
            )
        }
    }
}
