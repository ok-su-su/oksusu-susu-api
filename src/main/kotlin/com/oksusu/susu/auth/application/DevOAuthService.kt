package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.response.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.response.OauthTokenResponse
import com.oksusu.susu.client.oauth.kakao.KakaoOauthService
import org.springframework.stereotype.Service

@Service
class DevOAuthService(
    private val kakaoOauthService: KakaoOauthService,
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOauthLoginLinkDev(provider: OauthProvider): OauthLoginLinkResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthService.getOauthLoginLinkDev()
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOauthTokenDev(provider: OauthProvider, code: String): OauthTokenResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthService.getOauthTokenDev(code)
        }
    }
}
