package com.oksusu.susu.client.oauth.kakao

import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthTokenResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthUserInfoResponse
import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthWithdrawRequest
import com.oksusu.susu.client.oauth.kakao.model.KakaoOauthWithdrawResponse
import com.oksusu.susu.common.consts.BEARER
import com.oksusu.susu.common.consts.KAKAO_AK
import com.oksusu.susu.common.properties.KakaoOauthProperties
import com.oksusu.susu.config.webClient.SusuWebClient
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters

@Component
class KakaoClient(
    private val kakaoOauthProperties: KakaoOauthProperties,
    private val susuWebClient: SusuWebClient,
) {
    private val logger = mu.KotlinLogging.logger { }

    suspend fun kakaoTokenClient(
        redirectUrl: String,
        code: String,
    ): KakaoOauthTokenResponse {
        val url = kakaoOauthProperties.kauthUrl + String.format(
            kakaoOauthProperties.tokenUrl,
            kakaoOauthProperties.clientId,
            redirectUrl,
            code,
            kakaoOauthProperties.clientSecret
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
        return susuWebClient.webClient().get()
            .uri(kakaoOauthProperties.kapiUrl + kakaoOauthProperties.userInfoUrl)
            .header("Authorization", BEARER + accessToken)
            .retrieve()
            .bodyToMono(KakaoOauthUserInfoResponse::class.java)
            .awaitSingle()
    }

    suspend fun kakaoWithdrawClient(targetId: String): KakaoOauthWithdrawResponse? {
        val multiValueMap = LinkedMultiValueMap<String, String>().apply {
            setAll(
                mapOf(
                    "target_id_type" to "user_id",
                    "target_id" to targetId
                )
            ) }
        return susuWebClient.webClient().post()
            .uri(kakaoOauthProperties.kapiUrl + kakaoOauthProperties.unlinkUrl)
            .header("Authorization", KAKAO_AK + kakaoOauthProperties.adminKey)
            .body(BodyInserters.fromFormData(multiValueMap))
            .retrieve()
            .bodyToMono(KakaoOauthWithdrawResponse::class.java)
            .awaitSingle()
    }
}
