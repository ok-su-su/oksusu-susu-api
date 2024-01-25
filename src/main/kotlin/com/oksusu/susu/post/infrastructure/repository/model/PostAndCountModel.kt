package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.post.domain.Post
import com.querydsl.core.annotations.QueryProjection

class PostAndCountModel @QueryProjection constructor(
    val post: Post,
    val count: Count,
)
