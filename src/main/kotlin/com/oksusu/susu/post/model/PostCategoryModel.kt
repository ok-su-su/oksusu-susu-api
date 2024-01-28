package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.PostCategory

/** 게시글 카테고리 모델 */
data class PostCategoryModel(
    /** 카테고리 id */
    val id: Long,
    /** 카테고리 명 */
    val name: String,
    /** 카테고리 순서 */
    val seq: Int,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    var isActive: Boolean,
) {
    companion object {
        fun from(postCategory: PostCategory): PostCategoryModel {
            return PostCategoryModel(
                id = postCategory.id,
                name = postCategory.name,
                seq = postCategory.seq,
                isActive = postCategory.isActive
            )
        }
    }
}
