package com.oksusu.susu.client.oauth.kakao

import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse

interface KakaoClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
    ): KakaoOAuthTokenResponse

    suspend fun getUserInfo(
        accessToken: String,
    ): KakaoOAuthUserInfoResponse

    suspend fun withdraw(targetId: String): KakaoOAuthWithdrawResponse?
}
