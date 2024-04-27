package com.oksusu.susu.cache.model

data class ZSetModel<VALUE_TYPE>(
    val key: String?,
    val value: VALUE_TYPE?,
    val score: Double?,
)
