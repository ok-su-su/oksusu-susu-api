package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.user.domain.User
import com.querydsl.core.annotations.QueryProjection

class PostAndUserModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** user */
    val user: User
)