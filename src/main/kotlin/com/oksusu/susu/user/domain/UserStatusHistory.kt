package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.StatusAssignmentType
import jakarta.persistence.*

/** 유저 상태 변경 기록 */
@Entity
@Table(name = "user_status_history")
class UserStatusHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 해당 유저 id */
    val uid: Long,

    /** 변경된 유저 상태 타입 */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_assignment_type")
    val statusAssignmentType: StatusAssignmentType,

    /** 변경 이전 상태 id */
    @Column(name = "from_status_id")
    val fromStatusId: Long,

    /** 변경 후 상태 id */
    @Column(name = "to_status_id")
    val toStatusId: Long,
) : BaseEntity()
