package com.oksusu.susu.domain.post.infrastructure.repository.model

import com.oksusu.susu.domain.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

data class VoteOptionAndCountModel @QueryProjection constructor(
    /** vote option */
    val voteOption: VoteOption,
    /** vote option count 수 */
    val count: Long,
)
