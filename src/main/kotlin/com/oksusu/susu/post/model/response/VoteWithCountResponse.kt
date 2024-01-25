package com.oksusu.susu.post.model.response

import com.oksusu.susu.extension.equalsFromYearToSec
import com.oksusu.susu.post.infrastructure.repository.model.PostAndCountModel
import com.oksusu.susu.post.model.PostCategoryModel

data class VoteWithCountResponse(
    /** 투표 id */
    val id: Long,
    /** 카테고리 명 */
    val category: String,
    /** 내용 */
    val content: String,
    /** 총 투표 수 */
    val count: Long,
    /** 수정 여부 */
    val isModified: Boolean,
) {
    companion object {
        fun of(model: PostAndCountModel, postCategoryModel: PostCategoryModel): VoteWithCountResponse {
            return VoteWithCountResponse(
                id = model.post.id,
                category = postCategoryModel.name,
                content = model.post.content,
                count = model.count.count,
                isModified = model.post.createdAt.equalsFromYearToSec(model.post.modifiedAt)
            )
        }
    }
}
