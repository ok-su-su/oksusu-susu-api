package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.PenaltyType
import jakarta.persistence.*

/** 상태 정보 */
@Entity
@Table(name = "status")
class Status(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 상태 정보 설명 */
    val description: String,

    /** 패널티 보유 여부 / 패널티 있음 : 1, 패널티 없음 : 0 */
    @Column(name = "has_penalty")
    val hasPenalty: Boolean,

    /** 패널티 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "penalty_type")
    val penaltyType: PenaltyType?,

    /** 형량 */
    val degree: Long?,
) : BaseEntity()
