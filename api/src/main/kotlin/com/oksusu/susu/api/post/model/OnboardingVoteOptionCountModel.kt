package com.oksusu.susu.api.post.model

import com.oksusu.susu.api.post.domain.VoteOption

/** 온보딩용 투표 옵션 + 투표 수 모델 */
data class OnboardingVoteOptionCountModel(
    /** 옵션 내용 */
    val content: String,
    /** 투표 수 */
    val count: Long,
) {
    companion object {
        fun of(option: VoteOption, count: Long): OnboardingVoteOptionCountModel {
            return OnboardingVoteOptionCountModel(
                content = option.content,
                count = count
            )
        }
    }
}
