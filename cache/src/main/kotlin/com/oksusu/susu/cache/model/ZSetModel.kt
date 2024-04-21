package com.oksusu.susu.cache.model

class ZSetModel<VALUE_TYPE>(
    val key: String?,
    val value: VALUE_TYPE?,
    val score: Double?,
)
