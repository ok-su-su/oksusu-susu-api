package com.oksusu.susu.api.post.infrastructure.repository.model

import com.oksusu.susu.api.post.domain.Post
import com.oksusu.susu.api.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class PostAndVoteOptionModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** vote option */
    val voteOption: VoteOption,
)
