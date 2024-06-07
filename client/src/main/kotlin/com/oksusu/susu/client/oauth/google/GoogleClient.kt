package com.oksusu.susu.client.oauth.google

import com.oksusu.susu.client.oauth.google.model.GoogleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeysResponse

interface GoogleClient {
    suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): GoogleOAuthTokenResponse

    suspend fun getUserInfo(accessToken: String): GoogleOAuthUserInfoResponse

    suspend fun getOidcPublicKeys(): OidcPublicKeysResponse

    suspend fun withdraw(accessToken: String): String?
}
