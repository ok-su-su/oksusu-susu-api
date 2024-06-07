package com.oksusu.susu.api.auth.model

data class OidcDecodePayload(
    /** issuer ex https://kauth.kakao.com  */
    val iss: String,
    /** client id  */
    val aud: String,
    /** oauth provider account unique id  */
    val sub: String,
    val email: String,
//    val profile:
)
