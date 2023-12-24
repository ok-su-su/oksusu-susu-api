package com.oksusu.susu.community.model

import com.oksusu.susu.community.domain.VoteOption
import com.oksusu.susu.community.domain.vo.VoteOptionSummary


class VoteOptionCountModel(
    val id: Long,
    val communityId: Long,
    val content: String,
    val seq: Int,
    val count: Int,
) {
    companion object {
        fun of(option: VoteOption, summary: VoteOptionSummary): VoteOptionCountModel {
            return VoteOptionCountModel(
                id = option.id,
                communityId = option.communityId,
                content = option.content,
                seq = option.seq,
                count = summary.count
            )
        }
    }
}