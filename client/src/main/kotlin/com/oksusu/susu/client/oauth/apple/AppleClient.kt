package com.oksusu.susu.client.oauth.apple

import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeysResponse

interface AppleClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): AppleOAuthTokenResponse

    suspend fun getOidcPublicKeys(): OidcPublicKeysResponse

    suspend fun withdraw(
        clientId: String,
        clientSecret: String,
        token: String,
    )
}
