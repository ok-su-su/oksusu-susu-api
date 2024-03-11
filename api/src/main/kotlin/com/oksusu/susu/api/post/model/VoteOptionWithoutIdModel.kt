package com.oksusu.susu.api.post.model

import com.oksusu.susu.api.post.domain.VoteOption

/** 투표 옵션 내용 (id 제외) 모델 */
data class VoteOptionWithoutIdModel(
    /** 옵션 내용 */
    val content: String,
    /** 순서 */
    val seq: Int,
) {
    companion object {
        fun from(option: VoteOption): VoteOptionWithoutIdModel {
            return VoteOptionWithoutIdModel(
                content = option.content,
                seq = option.seq
            )
        }
    }
}
