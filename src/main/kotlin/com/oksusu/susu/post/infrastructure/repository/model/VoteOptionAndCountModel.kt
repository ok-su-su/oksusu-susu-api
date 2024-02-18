package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class VoteOptionAndCountModel @QueryProjection constructor(
    /** vote option */
    val voteOption: VoteOption,
    /** vote option count 수 */
    val count: Long,
)