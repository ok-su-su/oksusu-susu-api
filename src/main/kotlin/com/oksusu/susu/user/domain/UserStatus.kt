package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

/** 유저 상태 정보 */
@Entity
@Table(name = "user_status")
class UserStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 해당 유저 id */
    val uid: Long,

    /** 계정 상태 id */
    @Column(name = "account_status_id")
    var accountStatusId: Long,

    /** 커뮤니티 활동 상태 id */
    @Column(name = "community_status_id")
    var communityStatusId: Long,
) : BaseEntity()
