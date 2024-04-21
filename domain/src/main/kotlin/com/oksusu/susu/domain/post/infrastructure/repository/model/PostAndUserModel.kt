package com.oksusu.susu.domain.post.infrastructure.repository.model

import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.user.domain.User
import com.querydsl.core.annotations.QueryProjection

class PostAndUserModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** user */
    val user: User,
)
