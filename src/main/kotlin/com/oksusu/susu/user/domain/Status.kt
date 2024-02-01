package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.PenaltyType
import com.oksusu.susu.user.domain.vo.StatusType
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

    /** 상태 정보 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_type")
    val statusType: StatusType,

    /** 패널티 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "penalty_type")
    val penaltyType: PenaltyType?,

    /** 형량 */
    val degree: Long?,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()
