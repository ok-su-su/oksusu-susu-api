package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.dto.OauthRegisterRequest
import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Embedded
    val oauthInfo: OauthInfo,

    val name: String,
    val age: Int?,
    val birth: LocalDate?,
) : BaseEntity() {
    companion object {
        fun toEntity(oauthRegisterRequest: OauthRegisterRequest, oauthInfo: OauthInfo): User {
            return User(
                oauthInfo = oauthInfo,
                name = oauthRegisterRequest.name,
                age = oauthRegisterRequest.age,
                birth = oauthRegisterRequest.getBirth(),
            )
        }
    }
}
