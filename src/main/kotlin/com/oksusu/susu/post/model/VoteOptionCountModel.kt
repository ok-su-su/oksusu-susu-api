package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.VoteOptionSummary

class VoteOptionCountModel(
    val id: Long,
    val postId: Long,
    val content: String,
    val seq: Int,
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
