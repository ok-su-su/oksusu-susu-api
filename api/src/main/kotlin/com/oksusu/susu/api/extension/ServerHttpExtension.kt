package com.oksusu.susu.api.extension

import org.springframework.http.server.reactive.ServerHttpRequest

val ServerHttpRequest.remoteIp
    get() = headers.getFirst("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: this.remoteAddress?.address?.hostAddress
        ?: ""

val ServerHttpRequest.requestParam
    get() = this.queryParams.map { param -> "${param.key} : ${param.value}" }
        .joinToString("\n")
