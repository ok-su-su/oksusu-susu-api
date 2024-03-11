package com.oksusu.susu.domain.user.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.user.domain.vo.UserBlockTargetType
import jakarta.persistence.*

/** 차단 */
@Entity
@Table(name = "user_block")
class UserBlock(
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
    val targetType: UserBlockTargetType,

    /** 차단 이유 */
    val reason: String?,
) : BaseEntity()
