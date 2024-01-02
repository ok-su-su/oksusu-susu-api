package com.oksusu.susu.user.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.user.model.request.UpdateUserInfoRequest
import com.oksusu.susu.user.model.response.UserInfoResponse
import com.oksusu.susu.user.model.response.UserOAuthInfoResponse
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val userService: UserService,
    private val txTemplate: TransactionTemplates,
) {
    suspend fun getUserInfo(user: AuthUser): UserInfoResponse {
        return userService.findByIdOrThrow(user.id).run { UserInfoResponse.from(this) }
    }

    suspend fun updateUserInfo(user: AuthUser, request: UpdateUserInfoRequest): UserInfoResponse {
        val user = userService.findByIdOrThrow(user.id)

        val updatedUser = txTemplate.writer.executeWithContext {
            user.apply {
                name = request.name
                gender = request.gender
                birth = request.getBirth()
            }.run { userService.saveSync(this) }
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_USER_ERROR)

        return UserInfoResponse.from(updatedUser)
    }

    suspend fun getOAuthInfo(user: AuthUser): UserOAuthInfoResponse {
        return userService.findByIdOrThrow(user.id).run { UserOAuthInfoResponse.from(this) }
    }
}
