package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.dto.request.OauthRegisterRequest
import com.oksusu.susu.common.domain.BaseEntity
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

    var age: Int? = null,

    var birth: LocalDate? = null,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,
) : BaseEntity() {

    companion object {
        fun toEntity(oauthRegisterRequest: OauthRegisterRequest, oauthInfo: OauthInfo): User {
            return User(
                oauthInfo = oauthInfo,
                name = oauthRegisterRequest.name,
                age = oauthRegisterRequest.age,
                birth = oauthRegisterRequest.getBirth()
            )
        }
    }
}
