package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.VoteOptionSummary

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
    val count: Int,
) {
    companion object {
        fun of(option: VoteOption, summary: VoteOptionSummary): VoteOptionCountModel {
            return VoteOptionCountModel(
                id = option.id,
                postId = option.postId,
                content = option.content,
                seq = option.seq,
                count = summary.count
            )
        }
    }
}
