package com.oksusu.susu.block.domain

import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "block")
class Block(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val uid: Long,

    @Column(name = "target_id")
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: BlockTargetType,

    val reason: String?,
) : BaseEntity()
