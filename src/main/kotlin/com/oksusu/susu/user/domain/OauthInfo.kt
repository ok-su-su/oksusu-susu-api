package com.oksusu.susu.user.domain

import com.oksusu.susu.auth.model.OauthProvider
import jakarta.persistence.Embeddable

@Embeddable
class OauthInfo(
    val oauthProvider: OauthProvider,
    val oid: String
)
