package com.oksusu.susu.api.post.model.response

import com.oksusu.susu.api.post.model.OnboardingVoteOptionCountModel

data class OnboardingVoteResponse(
    /** 투표 옵션 */
    val options: List<OnboardingVoteOptionCountModel>,
)
