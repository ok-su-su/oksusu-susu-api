package com.oksusu.susu.auth.model.response

import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.user.domain.User

class UserOAuthInfoResponse(
    /** 유저 id */
    val id: Long,
    /** 유저 oauth provider */
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
