package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.helper.KakaoOauthHelper
import com.oksusu.susu.auth.model.AuthUserImpl
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.OauthLoginLinkResponse
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.user.infrastructure.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OauthService(
    private val kakaoOauthHelper: KakaoOauthHelper
) {

    /** oauth login link 가져오기 */
    fun getOauthLoginLinkDev(provider: OauthProvider): OauthLoginLinkResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getOauthLoginLinkDev()
        }
    }

//    /** idtoken 가져오기 *  */
//    fun getCredential(provider: OauthProvider, code: String, referer: String?): OauthTokenResponse {
//        return when (provider) {
//            OauthProvider.KAKAO -> TODO()
//        }
//    }
//
//    fun getCredentialDev(provider: OauthProvider, code: String): OauthTokenResponse {
//        return when (provider) {
//            OauthProvider.KAKAO -> TODO()
//        }
//    }
//
//    /** 회원탈퇴 *  */
//    fun withdraw(
//        provider: OauthProvider, oid: String?, appleAccessToken: String?, referer: String?,
//    ) {
//        return when (provider) {
//            OauthProvider.KAKAO -> TODO()
//        }
//    }
//
//    fun withdrawDev(provider: OauthProvider, oid: String?, appleAccessToken: String?) {
//        when (provider) {
//            KAKAO -> kakaoOauthHelper.withdrawKakaoOauthUser(oid)
//        }
//    }
}
