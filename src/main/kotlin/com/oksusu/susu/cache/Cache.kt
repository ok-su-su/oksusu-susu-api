package com.oksusu.susu.cache

import com.fasterxml.jackson.core.type.TypeReference
import java.time.Duration

sealed class Cache<VALUE_TYPE>(
    open val key: String,
    open val type: TypeReference<VALUE_TYPE>,
    open val duration: Duration,
)
