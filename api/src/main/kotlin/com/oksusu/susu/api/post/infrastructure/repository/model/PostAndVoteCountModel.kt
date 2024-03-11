package com.oksusu.susu.api.post.infrastructure.repository.model

import com.oksusu.susu.api.post.domain.Post
import com.querydsl.core.annotations.QueryProjection

class PostAndVoteCountModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** vote count 수 */
    val voteCount: Long,
)
