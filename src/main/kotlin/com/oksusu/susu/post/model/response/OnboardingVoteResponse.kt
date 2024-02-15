package com.oksusu.susu.post.model.response

import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.model.OnboardingVoteOptionCountModel
import com.oksusu.susu.post.model.VoteOptionCountModel

class OnboardingVoteResponse(
    /** 투표 옵션 */
    val options: List<OnboardingVoteOptionCountModel>,
)