package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.OauthProvider
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class OauthInfo(
    @Column(name = "oauth_provider")
    @Enumerated(EnumType.STRING)
    val oauthProvider: OauthProvider,

    @Column(name = "oauth_id")
    val oauthId: String,
)
