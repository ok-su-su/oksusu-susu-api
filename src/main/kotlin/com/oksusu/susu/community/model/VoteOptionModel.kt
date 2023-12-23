package com.oksusu.susu.community.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.oksusu.susu.community.domain.VoteOption
import io.swagger.v3.oas.annotations.media.Schema

class VoteOptionModel(
    @Schema(hidden = true)
    val id: Long?,
    @Schema(hidden = true)
    val communityId: Long?,
    val content: String,
    val seq: Int,
) {
    companion object {
        fun from(option: VoteOption): VoteOptionModel {
            return VoteOptionModel(
                id = option.id,
                communityId = option.communityId,
                content = option.content,
                seq = option.seq
            )
        }
    }
}