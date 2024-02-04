package com.oksusu.susu.post.infrastructure.repository.model

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteOption
import com.querydsl.core.annotations.QueryProjection

class PostAndVoteOptionAndOptionCountModel @QueryProjection constructor(
    /** post */
    val post: Post,
    /** vote option */
    val voteOption: VoteOption,
    /** vote option count 수 */
    val optionCount: Long,
)
