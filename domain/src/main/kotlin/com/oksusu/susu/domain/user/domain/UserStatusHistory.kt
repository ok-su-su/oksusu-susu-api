package com.oksusu.susu.domain.user.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
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
    val statusAssignmentType: UserStatusAssignmentType,

    /** 변경 이전 상태 id */
    @Column(name = "from_status_id")
    val fromStatusId: Long,

    /** 변경 후 상태 id */
    @Column(name = "to_status_id")
    val toStatusId: Long,

    /** 관리자 실행 여부, 1 : 관리자, 0 : 유저 */
    @Column(name = "is_forced")
    val isForced: Boolean = false,
) : BaseEntity()
