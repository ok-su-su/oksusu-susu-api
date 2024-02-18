package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.Gender
import com.oksusu.susu.user.domain.vo.OauthInfo
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
) : BaseEntity() {
    companion object {
        fun toEntity(request: OAuthRegisterRequest, oauthInfo: OauthInfo): User {
            return User(
                oauthInfo = oauthInfo,
                name = request.name,
                gender = request.gender,
                birth = request.getBirth()
            )
        }
    }
}
