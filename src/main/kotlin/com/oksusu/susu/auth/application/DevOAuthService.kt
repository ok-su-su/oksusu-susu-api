package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.application.oauth.KakaoOAuthService
import com.oksusu.susu.auth.model.OAuthProvider
import com.oksusu.susu.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.auth.model.response.OAuthTokenResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DevOAuthService(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    private val logger = KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOAuthLoginLinkDev(provider: OAuthProvider): OAuthLoginLinkResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthLoginLinkDev()
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOAuthTokenDev(provider: OAuthProvider, code: String): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthTokenDev(code)
        }
    }
}
