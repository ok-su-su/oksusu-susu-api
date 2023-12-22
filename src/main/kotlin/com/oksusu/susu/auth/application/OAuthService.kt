package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.helper.KakaoOauthHelper
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.response.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.response.OauthTokenResponse
import com.oksusu.susu.user.domain.OauthInfo
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoOauthHelper: KakaoOauthHelper,
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOauthLoginLink(provider: OauthProvider, uri: String): OauthLoginLinkResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getOauthLoginLink(uri)
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOauthToken(
        provider: OauthProvider,
        code: String,
        request: AbstractServerHttpRequest,
    ): OauthTokenResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getOauthToken(code, request.uri.toString())
        }
    }

    /** oauth 유저 정보 가져오기 */
    suspend fun getOauthUserInfo(
        provider: OauthProvider,
        accessToken: String,
    ): OauthInfo {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getKakaoUserInfo(accessToken)
        }.oauthInfo
    }

    /** oauth 유저 회원 탈퇴하기 */
    suspend fun withdraw(oauthInfo: OauthInfo) {
        when (oauthInfo.oauthProvider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.withdraw(oauthInfo.oauthId)
        }
    }
}
