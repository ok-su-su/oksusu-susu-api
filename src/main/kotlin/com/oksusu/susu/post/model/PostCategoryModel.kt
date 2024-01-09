package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.PostCategory

data class PostCategoryModel(
    val id: Long,
    val name: String,
    val seq: Int,
    val isActive: Boolean,
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
