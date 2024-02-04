package com.oksusu.susu.post.model

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.post.domain.VoteOption

/** 투표 옵션 + 투표 수 모델 */
data class VoteOptionCountModel(
    /** 투표 옵션 id */
    val id: Long,
    /** 투표 id */
    val postId: Long,
    /** 옵션 내용 */
    val content: String,
    /** 순서 */
    val seq: Int,
    /** 투표 수 */
    val count: Long,
    /** 투표 여부 */
    val isVoted: Boolean,
) {
    companion object {
        fun of(option: VoteOption, count: Long, isVoted: Boolean): VoteOptionCountModel {
            return VoteOptionCountModel(
                id = option.id,
                postId = option.postId,
                content = option.content,
                seq = option.seq,
                count = count,
                isVoted = isVoted
            )
        }
    }
}
