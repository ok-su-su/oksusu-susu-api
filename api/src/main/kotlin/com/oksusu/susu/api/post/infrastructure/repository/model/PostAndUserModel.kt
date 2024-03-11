package com.oksusu.susu.api.post.infrastructure.repository.model

import com.oksusu.susu.api.post.domain.Post
import com.oksusu.susu.api.user.domain.User
import com.querydsl.core.annotations.QueryProjection

class PostAndUserModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** user */
    val user: User,
)
