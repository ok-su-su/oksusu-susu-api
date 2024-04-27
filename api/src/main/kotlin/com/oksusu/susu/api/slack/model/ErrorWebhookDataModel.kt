package com.oksusu.susu.api.slack.model

import org.springframework.http.server.reactive.ServerHttpRequest

data class ErrorWebhookDataModel(
    val request: ServerHttpRequest,
    val exception: Exception,
)
