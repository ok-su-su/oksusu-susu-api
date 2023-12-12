package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.helper.KakaoOauthHelper
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.AbleRegisterResponse
import com.oksusu.susu.auth.model.dto.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.OauthTokenResponse
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class OauthService(
    private val kakaoOauthHelper: KakaoOauthHelper
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOauthLoginLinkDev(provider: OauthProvider): OauthLoginLinkResponse = when (provider) {
        OauthProvider.KAKAO -> kakaoOauthHelper.getOauthLoginLinkDev()
    }

    /** oauth token 가져오기 */
    suspend fun getOauthTokenDev(provider: OauthProvider, code: String): OauthTokenResponse {
        return withContext(Dispatchers.IO) {
            val tokenDeferred = async {
                when (provider) {
                    OauthProvider.KAKAO -> kakaoOauthHelper.getOauthTokenDev(code)
                }
            }
            tokenDeferred.await()
        }
    }

    /** 회원가입 가능 여부 체크. */
    suspend fun checkRegisterValid(provider: OauthProvider, accessToken: String): AbleRegisterResponse =
        when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.checkRegisterValid(accessToken)
        }
}
