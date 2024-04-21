package com.oksusu.susu.client.oauth.google

import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthTokenResponse
import com.oksusu.susu.client.oauth.google.model.GoogleOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class SuspendableGoogleClient(
    private val webClient: WebClient,
    private val googleOAuthUrlConfig: OAuthUrlConfig.GoogleOAuthUrlConfig,
): GoogleClient {
    val logger = KotlinLogging.logger {  }

    override suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): GoogleOAuthTokenResponse {
        val url = googleOAuthUrlConfig.oauth2GoogleApiUrl + googleOAuthUrlConfig.tokenUrl

        val formParams = "code=$code&client_id=$clientId&client_secret=$clientSecret&redirect_uri=$redirectUrl&grant_type=authorization_code"

        return webClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formParams)
            .retrieve()
            .bodyToMono(GoogleOAuthTokenResponse::class.java)
            .awaitSingle()

    }

    override suspend fun getUserInfo(accessToken: String): GoogleOAuthUserInfoResponse {
        val url = googleOAuthUrlConfig.googleApiUrl + String.format(
            googleOAuthUrlConfig.userInfoUrl,
            accessToken
        )

        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(GoogleOAuthUserInfoResponse::class.java)
            .awaitSingle()
    }

    override suspend fun withdraw(accessToken: String): String? {
        val url = googleOAuthUrlConfig.oauth2GoogleApiUrl + String.format(
            googleOAuthUrlConfig.revokeUrl,
            accessToken
        )

        return webClient.post()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
    }

}
