package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.api.config.SusuApiConfig
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import org.springframework.stereotype.Service

@Service
class AuthValidateService(
    private val userConfig: SusuApiConfig.UserConfig,
) {
    fun validateRegisterRequest(request: OAuthRegisterRequest) {
        val userCreateForm = userConfig.createForm

        if (request.name.length !in userCreateForm.minNameLength..userCreateForm.maxNameLength) {
            throw InvalidRequestException(ErrorCode.INVALID_USER_NAME_ERROR)
        }

        if (request.birth != null && request.birth < userCreateForm.minBirthYear) {
            throw InvalidRequestException(ErrorCode.INVALID_USER_BIRTH_ERROR)
        }
    }
}
