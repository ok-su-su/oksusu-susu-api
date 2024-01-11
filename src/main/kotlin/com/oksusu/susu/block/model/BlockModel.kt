package com.oksusu.susu.block.model

import com.oksusu.susu.block.domain.vo.BlockTargetType
import jakarta.persistence.*

// TODO : DTO data class 이용
class BlockModel(
    val id: Long,
    val uid: Long,
    val targetId: Long,
    val targetType: BlockTargetType,
    val reason: String?,
)
