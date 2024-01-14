package com.oksusu.susu.block.model

import com.oksusu.susu.block.domain.vo.BlockTargetType
import jakarta.persistence.*

class BlockModel(
    val id: Long,
    val uid: Long,
    val targetId: Long,
    val targetType: BlockTargetType,
    val reason: String?,
)
