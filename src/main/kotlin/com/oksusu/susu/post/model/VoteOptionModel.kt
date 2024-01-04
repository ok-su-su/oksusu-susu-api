package com.oksusu.susu.post.model

import com.oksusu.susu.post.domain.VoteOption
import io.swagger.v3.oas.annotations.media.Schema

class VoteOptionModel(
    @Schema(hidden = true)
    val id: Long?,
    @Schema(hidden = true)
    val postId: Long?,
    val content: String,
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
