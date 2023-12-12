package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val nickname: String,

    @Embedded
    val oauthInfo: OauthInfo,
) : BaseEntity()
