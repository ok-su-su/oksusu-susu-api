package com.oksusu.susu.cache.model.vo

data class OidcPublicKeyCacheModel(
    val kid: String,
    val alg: String,
    val use: String,
    val n: String,
    val e: String,
)
