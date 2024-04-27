package com.oksusu.susu.domain.post.infrastructure.repository.model

import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

data class PostAndVoteOptionModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** vote option */
    val voteOption: VoteOption,
)
