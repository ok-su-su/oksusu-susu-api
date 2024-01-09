package com.oksusu.susu.post.model.request

data class CreateVoteHistoryRequest(
    val isCancel: Boolean,
    val optionId: Long,
)
