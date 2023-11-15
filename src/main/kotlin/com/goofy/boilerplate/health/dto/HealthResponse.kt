package com.goofy.boilerplate.health.dto

import java.time.LocalDateTime

data class HealthResponse(
    val message: String,
    val dateTime: LocalDateTime,
    val profile: String,
)
