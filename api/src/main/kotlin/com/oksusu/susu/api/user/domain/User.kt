package com.oksusu.susu.api.user.domain

import com.oksusu.susu.api.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.api.domain.BaseEntity
import com.oksusu.susu.api.user.domain.vo.AccountRole
import com.oksusu.susu.api.user.domain.vo.Gender
import com.oksusu.susu.api.user.domain.vo.OauthInfo
import jakarta.persistence.*
import java.time.LocalDate

/** 유저 */
@Entity
@Table(name = "user")
class User(
    /** user id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** oauth 정보 */
    @Embedded
    var oauthInfo: OauthInfo,

    /** 이름 */
    var name: String,

    /** 성별 */
    @Enumerated(EnumType.ORDINAL)
    var gender: Gender? = null,

    /** 생년월일 */
    var birth: LocalDate? = null,

    /** 프로필 이미지 */
    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    /**
     * 계정 권한
     */
    @Enumerated(EnumType.STRING)
    val role: AccountRole,
) : BaseEntity() {
    companion object {
        fun toUserEntity(request: OAuthRegisterRequest, oauthInfo: OauthInfo): User {
            return User(
                oauthInfo = oauthInfo,
                name = request.name,
                gender = request.gender,
                birth = request.getBirth(),
                role = AccountRole.USER
            )
        }
    }
}
