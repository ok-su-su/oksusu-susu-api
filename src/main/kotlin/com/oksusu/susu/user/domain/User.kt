package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.request.OauthRegisterRequest
import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.domain.vo.Gender
import com.oksusu.susu.user.domain.vo.UserState
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Embedded
    var oauthInfo: OauthInfo,

    @Column(name = "user_state")
    @Enumerated(EnumType.ORDINAL)
    var userState: UserState = UserState.ACTIVE,

    var name: String,

    @Enumerated(EnumType.ORDINAL)
    var gender: Gender? = null,

    var birth: LocalDate? = null,

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
