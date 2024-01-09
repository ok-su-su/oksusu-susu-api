package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.vo.PostType

data class PostModel(
    val id: Long,
    val uid: Long,
    val type: PostType,
    val title: String?,
    val content: String,
    val isActive: Boolean,
)
