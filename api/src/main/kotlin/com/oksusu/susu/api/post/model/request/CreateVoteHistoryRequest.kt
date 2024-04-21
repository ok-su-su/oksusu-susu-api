package com.oksusu.susu.api.post.model.request

data class CreateVoteHistoryRequest(
    /** 투표 취소 여부 / 취소 : true, 투표하기 : false */
    val isCancel: Boolean,
    /** 투표 옵션 id */
    val optionId: Long,
)
