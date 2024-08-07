package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.application.oauth.AppleOAuthService
import com.oksusu.susu.api.auth.application.oauth.GoogleOAuthService
import com.oksusu.susu.api.auth.application.oauth.KakaoOAuthService
import com.oksusu.susu.api.auth.model.response.OAuthLoginLinkResponse
import com.oksusu.susu.api.auth.model.response.OAuthTokenResponse
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoOAuthService: KakaoOAuthService,
    private val appleOAuthService: AppleOAuthService,
    private val googleOAuthService: GoogleOAuthService,
) {
    private val logger = KotlinLogging.logger { }

    /** oauth withdraw 페이지용 login link 가져오기 */
    suspend fun getOAuthWithdrawLoginLink(provider: OAuthProvider, uri: String): OAuthLoginLinkResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthWithdrawLoginLink(uri)
            OAuthProvider.APPLE -> appleOAuthService.getOAuthWithdrawLoginLink(uri)
            OAuthProvider.GOOGLE -> googleOAuthService.getOAuthWithdrawLoginLink(uri)
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOAuthWithdrawToken(
        provider: OAuthProvider,
        code: String,
    ): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthWithdrawToken(code)
            OAuthProvider.APPLE -> appleOAuthService.getOAuthWithdrawToken(code)
            OAuthProvider.GOOGLE -> googleOAuthService.getOAuthWithdrawToken(code)
        }
    }

    /** oauth info 가져오기 */
    suspend fun getOAuthInfo(
        provider: OAuthProvider,
        accessToken: String,
    ): OauthInfo {
        return when (provider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.getOAuthInfo(accessToken)
            OAuthProvider.APPLE -> appleOAuthService.getOAuthInfo(accessToken)
            OAuthProvider.GOOGLE -> googleOAuthService.getOAuthInfo(accessToken)
        }.oauthInfo
    }

    /** oauth 유저 회원 탈퇴하기 */
    suspend fun withdraw(oauthInfo: OauthInfo, code: String?, googleAccessToken: String?, appleAccessToken: String?) {
        when (oauthInfo.oAuthProvider) {
            OAuthProvider.KAKAO -> kakaoOAuthService.withdraw(oauthInfo.oAuthId)
            OAuthProvider.APPLE -> appleOAuthService.withdraw(appleAccessToken!!)
            OAuthProvider.GOOGLE -> googleOAuthService.withdraw(googleAccessToken!!)
        }
    }
}
