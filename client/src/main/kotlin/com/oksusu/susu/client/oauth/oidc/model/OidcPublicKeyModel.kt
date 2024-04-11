package com.oksusu.susu.client.oauth.oidc.model

class OidcPublicKeyModel(
    val kid: String,
    val alg: String,
    val use: String,
    val n: String,
    val e: String,
)
