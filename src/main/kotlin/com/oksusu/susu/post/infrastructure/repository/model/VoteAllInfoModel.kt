package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.user.domain.User
import com.querydsl.core.annotations.QueryProjection

class VoteAllInfoModel @QueryProjection constructor(
    val post: Post,
    val voteOption: VoteOption,
    val optionCount: Long,
    val creator: User
)
