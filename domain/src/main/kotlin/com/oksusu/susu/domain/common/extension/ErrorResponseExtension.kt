package com.oksusu.susu.domain.common.extension

import com.oksusu.susu.common.consts.FAIL_TO_VALIDATE_MESSAGE
import com.oksusu.susu.common.dto.ErrorResponse
import com.oksusu.susu.common.exception.ErrorCode
import org.hibernate.TypeMismatchException

fun ErrorResponse.of(e: TypeMismatchException): ErrorResponse = ErrorResponse(
    errorCode = ErrorCode.BAD_REQUEST_ERROR.name,
    reason = e.message ?: FAIL_TO_VALIDATE_MESSAGE
)
