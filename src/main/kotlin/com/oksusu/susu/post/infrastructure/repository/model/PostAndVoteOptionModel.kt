package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class PostAndVoteOptionModel @QueryProjection constructor(
    val post: Post,
    val voteOption: VoteOption,
)
