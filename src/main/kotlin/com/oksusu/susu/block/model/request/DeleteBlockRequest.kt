package com.oksusu.susu.block.model.request

import com.oksusu.susu.block.domain.vo.BlockTargetType

class DeleteBlockRequest(
    /** 차단 타겟 id */
    val targetId: Long,
    /** 차단 타겟 타입 */
    val targetType: BlockTargetType,
)
