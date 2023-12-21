package com.oksusu.susu.exception

open class SusuException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.description,
    val extra: Map<String, Any>? = null,
) : RuntimeException(message ?: errorCode.description)

class NotFoundException(errorCode: ErrorCode) : SusuException(errorCode)

class InvalidTokenException(errorCode: ErrorCode) : SusuException(errorCode)

class InvalidRequestException(errorCode: ErrorCode) : SusuException(errorCode)

class FailToCreateException(errorCode: ErrorCode) : SusuException(errorCode)
