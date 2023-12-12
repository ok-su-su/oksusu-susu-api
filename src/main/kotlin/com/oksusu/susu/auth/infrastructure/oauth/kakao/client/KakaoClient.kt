package com.oksusu.susu.auth.infrastructure.oauth.kakao.client

import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthTokenResponse
import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthUserInfoResponse
import com.oksusu.susu.common.consts.BEARER
import com.oksusu.susu.common.properties.KakaoOauthProperties
import com.oksusu.susu.config.webClient.SusuWebClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component

private const val KAKAO_TOKEN_URL = "/oauth/token?grant_type=authorization_code&client_id=%s&redirect_uri=%s&code=%s&client_secret=%s"
private const val KAKAO_KAUTH_URL = "https://kauth.kakao.com"
private const val KAKAO_KAPI_URL = "https://kapi.kakao.com"

@Component
class KakaoClient(
    private val kakaoOauthProperties: KakaoOauthProperties,
    private val susuWebClient: SusuWebClient
) {
    private val logger = mu.KotlinLogging.logger { }

    suspend fun kakaoTokenClient(
        redirectUrl: String,
        code: String,
    ): KakaoOauthTokenResponse {
        val url =
            KAKAO_KAUTH_URL + String.format(
                KAKAO_TOKEN_URL,
                kakaoOauthProperties.clientId,
                redirectUrl,
                code,
                kakaoOauthProperties.clientSecret,
            )
        return susuWebClient.webClient().post()
            .uri(url)
            .retrieve()
            .bodyToMono(KakaoOauthTokenResponse::class.java)
            .awaitSingle()
    }

    suspend fun kakaoUserInfoClient(
        accessToken: String,
    ): KakaoOauthUserInfoResponse {
        val url = "$KAKAO_KAPI_URL/v2/user/me"
        return susuWebClient.webClient().get()
            .uri(url)
            .header("Authorization", BEARER + accessToken)
            .retrieve()
            .bodyToMono(KakaoOauthUserInfoResponse::class.java)
            .awaitSingle()
    }

}
