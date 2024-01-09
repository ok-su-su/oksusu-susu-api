package com.oksusu.susu.user.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.user.model.request.UpdateUserInfoRequest
import com.oksusu.susu.user.model.response.UserInfoResponse
import org.springframework.stereotype.Service

@Service
class UserFacade(
    private val userService: UserService,
    private val txTemplate: TransactionTemplates,
) {
    suspend fun getUserInfo(user: AuthUser): UserInfoResponse {
        return userService.findByIdOrThrow(user.id)
            .run { UserInfoResponse.from(this) }
    }

    suspend fun updateUserInfo(uid: Long, user: AuthUser, request: UpdateUserInfoRequest): UserInfoResponse {
        user.isAuthorThrow(uid)

        val beforeChangedUser = userService.findByIdOrThrow(user.id)

        val updatedUser = txTemplate.writer.coExecute {
            beforeChangedUser.apply {
                this.name = request.name
                this.gender = request.gender
                this.birth = request.getBirth()
            }.run { userService.saveSync(this) }
        }

        return UserInfoResponse.from(updatedUser)
    }
}
