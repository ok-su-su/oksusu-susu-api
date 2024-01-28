package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.VoteOption

/** 투표 옵션 모델 */
data class VoteOptionModel(
    /** 투표 옵션 id */
    val id: Long,
    /** 투표 id */
    val postId: Long,
    /** 옵션 내용 */
    val content: String,
    /** 순서 */
    val seq: Int,
) {
    companion object {
        fun from(option: VoteOption): VoteOptionModel {
            return VoteOptionModel(
                id = option.id,
                postId = option.postId,
                content = option.content,
                seq = option.seq
            )
        }
    }
}
