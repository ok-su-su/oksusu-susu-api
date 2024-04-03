package com.oksusu.susu.client.oauth.kakao

import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOAuthWithdrawResponse
import com.oksusu.susu.common.consts.BEARER
import com.oksusu.susu.common.consts.KAKAO_AK
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

class SuspendableKakaoClient(
    private val webClient: WebClient,
    private val kakaoOAuthUrlConfig: OAuthUrlConfig.KakaoOAuthUrlConfig,
) : KakaoClient {
    private val logger = KotlinLogging.logger { }

    override suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): KakaoOAuthTokenResponse {
        val url = kakaoOAuthUrlConfig.kauthUrl + String.format(
            kakaoOAuthUrlConfig.tokenUrl,
            clientId,
            redirectUrl,
            code,
            clientSecret
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
            .uri(kakaoOAuthUrlConfig.kapiUrl + kakaoOAuthUrlConfig.userInfoUrl)
            .header("Authorization", BEARER + accessToken)
            .retrieve()
            .bodyToMono(KakaoOAuthUserInfoResponse::class.java)
            .awaitSingle()
    }

    override suspend fun withdraw(targetId: String, adminKey: String): KakaoOAuthWithdrawResponse? {
        val multiValueMap = LinkedMultiValueMap<String, String>().apply {
            setAll(
                mapOf(
                    "target_id_type" to "user_id",
                    "target_id" to targetId
                )
            )
        }
        return webClient.post()
            .uri(kakaoOAuthUrlConfig.kapiUrl + kakaoOAuthUrlConfig.unlinkUrl)
            .header("Authorization", KAKAO_AK + adminKey)
            .body(BodyInserters.fromFormData(multiValueMap))
            .retrieve()
            .bodyToMono(KakaoOAuthWithdrawResponse::class.java)
            .awaitSingle()
    }
}
