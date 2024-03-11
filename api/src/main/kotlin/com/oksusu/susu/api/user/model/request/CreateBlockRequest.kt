package com.oksusu.susu.api.user.model.request

import com.oksusu.susu.domain.user.domain.vo.UserBlockTargetType

class CreateBlockRequest(
    /** 차단 타겟 id */
    val targetId: Long,
    /** 차단 타겟 타입 */
    val targetType: UserBlockTargetType,
    /** 차단 이유 */
    val reason: String?,
)
