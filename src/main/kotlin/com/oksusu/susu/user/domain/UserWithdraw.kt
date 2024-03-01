package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.AccountRole
import com.oksusu.susu.user.domain.vo.OauthInfo
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
) : BaseEntity() {
    companion object {
        fun from(user: User): UserWithdraw {
            return UserWithdraw(
                uid = user.id,
                oauthInfo = user.oauthInfo,
                role = user.role
            )
        }
    }
}
