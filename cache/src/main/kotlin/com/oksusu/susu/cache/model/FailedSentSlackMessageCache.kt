package com.oksusu.susu.cache.model

import java.time.LocalDateTime

data class FailedSentSlackMessageCache(
    val token: String,
    val message: String,
    val failedAt: LocalDateTime = LocalDateTime.now(),
    val isStacked: Boolean = false,
)
