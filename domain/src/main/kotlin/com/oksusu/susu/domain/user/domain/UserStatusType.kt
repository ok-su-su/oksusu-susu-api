package com.oksusu.susu.domain.user.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import jakarta.persistence.*

/** 유저 상태 정보 타입 */
@Entity
@Table(name = "user_status_type")
class UserStatusType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 상태 정보 타입 정보 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_type_info")
    val statusTypeInfo: UserStatusTypeInfo,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    @Column(name = "is_active")
    var isActive: Boolean = true,
) : BaseEntity()
