package com.oksusu.susu.auth.model.dto.response

import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.user.domain.User

class UserOAuthInfoResponse(
    val id: Long,
    val oauthProvider: OauthProvider,
) {
    companion object {
        fun from(user: User): UserOAuthInfoResponse {
            return UserOAuthInfoResponse(
                id = user.id,
                oauthProvider = user.oauthInfo.oauthProvider
            )
        }
    }
}
