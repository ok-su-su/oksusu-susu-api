package com.oksusu.susu.block.model.request

import com.oksusu.susu.block.domain.vo.BlockTargetType

class CreateBlockRequest(
    val targetId: Long,
    val targetType: BlockTargetType,
    val reason: String?,
)
