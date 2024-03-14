package com.oksusu.susu.api.user.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.user.model.request.UpdateUserInfoRequest
import com.oksusu.susu.api.user.model.response.UserInfoResponse
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.config.database.TransactionTemplates
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val userService: UserService,
    private val txTemplate: TransactionTemplates,
    private val userValidateService: UserValidateService,
) {
    suspend fun getUserInfo(user: AuthUser): UserInfoResponse {
        return userService.findByIdOrThrow(user.uid)
            .run { UserInfoResponse.from(this) }
    }

    suspend fun updateUserInfo(uid: Long, user: AuthUser, request: UpdateUserInfoRequest): UserInfoResponse {
        user.isNotAuthorThrow(uid)
        userValidateService.validateUpdateUserRequest(request)

        val beforeChangedUser = userService.findByIdOrThrow(user.uid)

        val updatedUser = txTemplate.writer.coExecute(Dispatchers.IO) {
            beforeChangedUser.apply {
                this.name = request.name
                this.gender = request.gender
                this.birth = request.getBirth()
            }.run { userService.saveSync(this) }
        }

        return UserInfoResponse.from(updatedUser)
    }
}
