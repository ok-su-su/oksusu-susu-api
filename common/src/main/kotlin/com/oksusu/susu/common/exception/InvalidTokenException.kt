package com.oksusu.susu.common.exception

class InvalidTokenException(
    val errorCode: ErrorCode,
    message: String? = null,
    val extra: Map<String, Any>? = null,
) : RuntimeException(message ?: errorCode.description)
