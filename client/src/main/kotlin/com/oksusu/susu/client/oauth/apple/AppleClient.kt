package com.oksusu.susu.client.oauth.apple

import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse

interface AppleClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): AppleOAuthTokenResponse
//
//    suspend fun getUserInfo(accessToken: String): KakaoOAuthUserInfoResponse
//
//    suspend fun withdraw(targetId: String, adminKey: String): KakaoOAuthWithdrawResponse?
}
