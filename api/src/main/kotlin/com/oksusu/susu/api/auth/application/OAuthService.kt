package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.application.oauth.AppleOAuthService
import com.oksusu.susu.api.auth.application.oauth.KakaoOAuthService
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val appleOAuthService: AppleOAuthService,
) {
    private val logger = KotlinLogging.logger { }

    /** oauth withdraw 페이지용 login link 가져오기 */
    suspend fun getOAuthWithdrawLoginLink(provider: OAuthProvider, uri: String): OAuthLoginLinkResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthWithdrawLoginLink(uri)
            OAuthProvider.GOOGLE -> kakaoOAuthService.getOAuthWithdrawLoginLink(uri)
            OAuthProvider.APPLE -> appleOAuthService.getOAuthWithdrawLoginLink(uri)
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOAuthWithdrawToken(
        provider: OAuthProvider,
        code: String,
        request: ServerHttpRequest,
    ): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthWithdrawToken(code, request.uri.toString())
            OAuthProvider.GOOGLE -> kakaoOAuthService.getOAuthWithdrawToken(code, request.uri.toString())
            OAuthProvider.APPLE -> appleOAuthService.getOAuthWithdrawToken(code, request.uri.toString())
        }
    }

    /** oauth 유저 정보 가져오기 */
    suspend fun getOAuthUserInfo(
        provider: OAuthProvider,
        accessToken: String,
    ): OauthInfo {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getKakaoUserInfo(accessToken)
            OAuthProvider.GOOGLE -> kakaoOAuthService.getKakaoUserInfo(accessToken)
            OAuthProvider.APPLE -> appleOAuthService.getKakaoUserInfo(accessToken)
        }.oauthInfo
    }

    /** oauth 유저 회원 탈퇴하기 */
    suspend fun withdraw(oauthInfo: OauthInfo) {
        when (oauthInfo.oAuthProvider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.withdraw(oauthInfo.oAuthId)
            OAuthProvider.GOOGLE -> kakaoOAuthService.withdraw(oauthInfo.oAuthId)
            OAuthProvider.APPLE -> appleOAuthService.withdraw(oauthInfo.oAuthId)
        }
    }
}
