package com.oksusu.susu.community.model

import com.oksusu.susu.community.domain.vo.PostType

class PostModel(
    val id: Long,
    val uid: Long,
    val type: PostType,
    val title: String?,
    val content: String,
    val isActive: Boolean,
)
