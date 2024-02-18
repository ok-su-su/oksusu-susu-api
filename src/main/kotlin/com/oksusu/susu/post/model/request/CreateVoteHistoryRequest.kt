package com.oksusu.susu.post.model.request

import jakarta.validation.constraints.Positive

data class CreateVoteHistoryRequest(
    /** 투표 취소 여부 / 취소 : true, 투표하기 : false */
    val isCancel: Boolean,
    /** 투표 옵션 id */
    @field:Positive
    val optionId: Long,
)
