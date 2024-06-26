package com.oksusu.susu.api.user.model

/** 차단 유저, post id 제공하는 모델 */
data class UserAndPostBlockIdModel(
    /** 유저 차단 id */
    val userBlockIds: Set<Long>,
    /** 게시글 차단 id */
    val postBlockIds: Set<Long>,
)
