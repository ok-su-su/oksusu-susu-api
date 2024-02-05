package com.oksusu.susu.user.model.request

import com.oksusu.susu.user.domain.vo.UserBlockTargetType

class DeleteBlockRequest(
    /** 차단 타겟 id */
    val targetId: Long,
    /** 차단 타겟 타입 */
    val targetType: UserBlockTargetType,
)
