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
) {
    companion object {
        fun of(option: VoteOption, count: Count): VoteOptionCountModel {
            return VoteOptionCountModel(
                id = option.id,
                postId = option.postId,
                content = option.content,
                seq = option.seq,
                count = count.count
            )
        }
    }
}
