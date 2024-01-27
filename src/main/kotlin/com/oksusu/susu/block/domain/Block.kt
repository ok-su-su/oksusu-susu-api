package com.oksusu.susu.block.domain

import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

/** 차단 */
@Entity
@Table(name = "block")
class Block(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 차단하는 유저 id */
    val uid: Long,

    /** 차단 타겟 id */
    @Column(name = "target_id")
    val targetId: Long,

    /** 차단 타겟 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    val targetType: BlockTargetType,

    /** 차단 이유 */
    val reason: String?,
) : BaseEntity()
