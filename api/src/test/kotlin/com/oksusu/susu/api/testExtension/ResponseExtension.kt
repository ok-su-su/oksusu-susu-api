package com.oksusu.susu.api.testExtension

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import org.springframework.http.ResponseEntity

fun <T> ResponseEntity<T>.getBodyOrThrow() = this.body ?: throw InvalidRequestException(ErrorCode.BAD_REQUEST_ERROR)
