package com.oksusu.susu.auth.model.response

import com.oksusu.susu.auth.model.OAuthProvider
import com.oksusu.susu.user.domain.User

class UserOAuthInfoResponse(
    /** 유저 id */
    val id: Long,
    /** 유저 oauth provider */
    val oAuthProvider: OAuthProvider,
) {
    companion object {
        fun from(user: User): UserOAuthInfoResponse {
            return UserOAuthInfoResponse(
                id = user.id,
                oAuthProvider = user.oauthInfo.oAuthProvider
            )
        }
    }
}
