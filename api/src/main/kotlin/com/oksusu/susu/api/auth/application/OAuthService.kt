package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.application.oauth.KakaoOAuthService
import com.oksusu.susu.api.auth.model.OAuthProvider
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.api.user.domain.vo.OauthInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    private val logger = KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOAuthLoginLink(provider: OAuthProvider, uri: String): OAuthLoginLinkResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthLoginLink(uri)
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOAuthToken(
        provider: OAuthProvider,
        code: String,
        request: ServerHttpRequest,
    ): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthToken(code, request.uri.toString())
        }
    }

    /** oauth 유저 정보 가져오기 */
    suspend fun getOAuthUserInfo(
        provider: OAuthProvider,
        accessToken: String,
    ): OauthInfo {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getKakaoUserInfo(accessToken)
        }.oauthInfo
    }

    /** oauth 유저 회원 탈퇴하기 */
    suspend fun withdraw(oauthInfo: OauthInfo) {
        when (oauthInfo.oAuthProvider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.withdraw(oauthInfo.oAuthId)
        }
    }
}
