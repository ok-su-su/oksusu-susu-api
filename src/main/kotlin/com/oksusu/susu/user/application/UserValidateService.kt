package com.oksusu.susu.user.application

import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.user.model.request.UpdateUserInfoRequest
import org.springframework.stereotype.Service

@Service
class UserValidateService (
    private val userConfig: SusuConfig.UserConfig,
){
    fun validateUpdateUserRequest(request: UpdateUserInfoRequest){
        val userCreateForm = userConfig.createForm

        if (request.name.length !in userCreateForm.minNameLength .. userCreateForm.maxNameLength){
            throw InvalidRequestException(ErrorCode.INVALID_USER_NAME_ERROR)
        }

        if (request.birth != null && request.birth < userCreateForm.minBirthYear){
            throw InvalidRequestException(ErrorCode.INVALID_USER_BIRTH_ERROR)
        }
    }
}