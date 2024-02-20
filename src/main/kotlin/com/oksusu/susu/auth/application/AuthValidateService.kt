package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import org.springframework.stereotype.Service

@Service
class AuthValidateService(
    private val userConfig: SusuConfig.UserConfig,
) {
    fun validateRegisterRequest(request: OAuthRegisterRequest){
        val userCreateForm = userConfig.createForm

        if (request.name.length !in userCreateForm.minNameLength .. userCreateForm.maxNameLength){
            throw InvalidRequestException(ErrorCode.INVALID_USER_NAME_ERROR)
        }

        if (request.birth != null && request.birth < userCreateForm.minBirthYear){
            throw InvalidRequestException(ErrorCode.INVALID_USER_BIRTH_ERROR)
        }
    }
}