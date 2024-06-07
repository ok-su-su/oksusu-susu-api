package com.oksusu.susu.api.auth.model.request

data class OidcLoginRequest(
    /** oauth idToken */
    val idToken: String,
)
