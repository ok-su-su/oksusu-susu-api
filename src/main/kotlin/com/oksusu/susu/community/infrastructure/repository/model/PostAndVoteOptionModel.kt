package com.oksusu.susu.community.infrastructure.repository.model

import com.oksusu.susu.community.domain.Post
import com.oksusu.susu.community.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class PostAndVoteOptionModel @QueryProjection constructor(
    val post: Post,
    val voteOption: VoteOption,
)
