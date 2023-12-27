package com.oksusu.susu.community.infrastructure.repository.model

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class CommunityAndVoteOptionModel @QueryProjection constructor(
    val community: Community,
    val voteOption: VoteOption,
)
