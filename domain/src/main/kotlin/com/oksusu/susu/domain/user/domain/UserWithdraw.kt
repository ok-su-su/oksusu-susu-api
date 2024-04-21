package com.oksusu.susu.domain.user.domain

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import jakarta.persistence.*

/** 탈퇴 유저 정보 */
@Entity
@Table(name = "user_withdraw")
class UserWithdraw(
    /** user_withdraw id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 탈퇴 유저 id */
    val uid: Long,

    /** 탈퇴 유저 oauth 정보 */
    @Embedded
    var oauthInfo: OauthInfo,

    /**
     * 탈퇴 유저 계정 권한
     */
    @Enumerated(EnumType.STRING)
    val role: AccountRole,
) : BaseEntity()
