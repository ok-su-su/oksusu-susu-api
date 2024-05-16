package com.oksusu.susu.client.oauth.google

import com.oksusu.susu.client.oauth.google.model.GoogleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthUserInfoResponse

interface GoogleClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): GoogleOAuthTokenResponse

    suspend fun getUserInfo(accessToken: String): GoogleOAuthUserInfoResponse

    suspend fun withdraw(accessToken: String): String?
}
