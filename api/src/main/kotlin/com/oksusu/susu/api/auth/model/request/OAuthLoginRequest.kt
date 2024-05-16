package com.oksusu.susu.api.auth.model.request

data class OAuthLoginRequest(
    /** oauth access token */
    val accessToken: String,
)
