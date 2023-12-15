package com.oksusu.susu.auth.infrastructure.oauth.kakao.client

import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthTokenResponse
import com.oksusu.susu.auth.infrastructure.oauth.kakao.dto.KakaoOauthUserInfoResponse
import com.oksusu.susu.common.consts.BEARER
import com.oksusu.susu.common.properties.KakaoOauthProperties
import com.oksusu.susu.config.webClient.SusuWebClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component

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
            kakaoOauthProperties.kauthUrl + String.format(
                kakaoOauthProperties.tokenUrl,
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
        val url = "${kakaoOauthProperties.kapiUrl}/v2/user/me"
        return susuWebClient.webClient().get()
            .uri(url)
            .header("Authorization", BEARER + accessToken)
            .retrieve()
            .bodyToMono(KakaoOauthUserInfoResponse::class.java)
            .awaitSingle()
    }

}
