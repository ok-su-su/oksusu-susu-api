package com.oksusu.susu.api.user.model

import com.oksusu.susu.api.user.domain.vo.UserBlockTargetType

/** 차단 모델 */
class BlockModel(
    /** 차단 id */
    val id: Long,
    /** 차단하는 유저 id */
    val uid: Long,
    /** 차단 타겟 id */
    val targetId: Long,
    /** 차단 타겟 타입 */
    val targetType: UserBlockTargetType,
    /** 차단 이유 */
    val reason: String?,
)
