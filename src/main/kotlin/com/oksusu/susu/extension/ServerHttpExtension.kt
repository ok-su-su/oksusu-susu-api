package com.oksusu.susu.extension

import org.springframework.http.server.reactive.ServerHttpRequest

val ServerHttpRequest.remoteIp
    get() = headers.getFirst("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: this.remoteAddress?.address?.hostAddress
        ?: ""
