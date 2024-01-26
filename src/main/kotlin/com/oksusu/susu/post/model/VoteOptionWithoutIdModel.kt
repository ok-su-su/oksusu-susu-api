package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.VoteOption

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
