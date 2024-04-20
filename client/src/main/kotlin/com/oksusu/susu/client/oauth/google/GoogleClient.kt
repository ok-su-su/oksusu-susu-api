package com.oksusu.susu.client.oauth.google

import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse

interface GoogleClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): GoogleOAuthTokenResponse

    suspend fun getUserInfo(accessToken: String): GoogleOAuthUserInfoResponse
}
