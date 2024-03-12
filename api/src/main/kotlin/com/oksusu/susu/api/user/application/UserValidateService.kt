package com.oksusu.susu.api.user.application

import com.oksusu.susu.api.config.SusuApiConfig
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.api.user.model.request.UpdateUserInfoRequest
import org.springframework.stereotype.Service

@Service
class UserValidateService(
    private val userConfig: SusuApiConfig.UserConfig,
) {
    fun validateUpdateUserRequest(request: UpdateUserInfoRequest) {
        val userCreateForm = userConfig.createForm

        if (request.name.length !in userCreateForm.minNameLength..userCreateForm.maxNameLength) {
            throw InvalidRequestException(ErrorCode.INVALID_USER_NAME_ERROR)
        }

        if (request.birth != null && request.birth < userCreateForm.minBirthYear) {
            throw InvalidRequestException(ErrorCode.INVALID_USER_BIRTH_ERROR)
        }
    }
}
