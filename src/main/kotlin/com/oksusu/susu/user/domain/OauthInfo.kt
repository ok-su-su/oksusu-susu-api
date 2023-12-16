package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.OauthProvider
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class OauthInfo(
    @Enumerated(EnumType.STRING)
    val oauth_provider: OauthProvider,

    val oauth_id: String,
)
