package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.request.OauthRegisterRequest
import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.Gender
import com.oksusu.susu.user.domain.vo.UserState
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

    /** 유저 계정 상태 */
    @Column(name = "user_state")
    @Enumerated(EnumType.ORDINAL)
    var userState: UserState = UserState.ACTIVE,

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
) : BaseEntity() {
    companion object {
        fun toEntity(oauthRegisterRequest: OauthRegisterRequest, oauthInfo: OauthInfo): User {
            return User(
                oauthInfo = oauthInfo,
                name = oauthRegisterRequest.name,
                gender = oauthRegisterRequest.gender,
                birth = oauthRegisterRequest.getBirth()
            )
        }
    }
}
