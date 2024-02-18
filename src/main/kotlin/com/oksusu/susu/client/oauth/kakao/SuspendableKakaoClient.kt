package com.oksusu.susu.client.oauth.kakao

import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse
import com.oksusu.susu.common.consts.BEARER
import com.oksusu.susu.common.consts.KAKAO_AK
import com.oksusu.susu.config.OAuthConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

class SuspendableKakaoClient(
    private val webClient: WebClient,
    private val kakaoOAuthProperties: OAuthConfig.KakaoOAuthProperties,
) : KakaoClient {
    private val logger = KotlinLogging.logger { }

    override suspend fun getToken(
        redirectUrl: String,
        code: String,
    ): KakaoOAuthTokenResponse {
        val url = kakaoOAuthProperties.kauthUrl + String.format(
            kakaoOAuthProperties.tokenUrl,
            kakaoOAuthProperties.clientId,
            redirectUrl,
            code,
            kakaoOAuthProperties.clientSecret
        )
        return webClient.post()
            .uri(url)
            .retrieve()
            .bodyToMono(KakaoOAuthTokenResponse::class.java)
            .awaitSingle()
    }

    override suspend fun getUserInfo(
        accessToken: String,
    ): KakaoOAuthUserInfoResponse {
        return webClient.get()
            .uri(kakaoOAuthProperties.kapiUrl + kakaoOAuthProperties.userInfoUrl)
            .header("Authorization", BEARER + accessToken)
            .retrieve()
            .bodyToMono(KakaoOAuthUserInfoResponse::class.java)
            .awaitSingle()
    }

    override suspend fun withdraw(targetId: String): KakaoOAuthWithdrawResponse? {
        val multiValueMap = LinkedMultiValueMap<String, String>().apply {
            setAll(
                mapOf(
                    "target_id_type" to "user_id",
                    "target_id" to targetId
                )
            )
        }
        return webClient.post()
            .uri(kakaoOAuthProperties.kapiUrl + kakaoOAuthProperties.unlinkUrl)
            .header("Authorization", KAKAO_AK + kakaoOAuthProperties.adminKey)
            .body(BodyInserters.fromFormData(multiValueMap))
            .retrieve()
            .bodyToMono(KakaoOAuthWithdrawResponse::class.java)
            .awaitSingle()
    }
}
