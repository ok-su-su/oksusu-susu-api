package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.KakaoOauthHelper
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.auth.model.dto.request.OAuthLoginRequest
import com.oksusu.susu.auth.model.dto.request.OauthRegisterRequest
import com.oksusu.susu.auth.model.dto.response.AbleRegisterResponse
import com.oksusu.susu.auth.model.dto.response.OauthLoginLinkResponse
import com.oksusu.susu.auth.model.dto.response.OauthTokenResponse
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.user.application.UserService
import com.oksusu.susu.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OauthService(
    private val kakaoOauthHelper: KakaoOauthHelper,
    private val tokenGenerateHelper: TokenGenerateHelper,
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService,
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link 가져오기 */
    suspend fun getOauthLoginLinkDev(provider: OauthProvider): OauthLoginLinkResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getOauthLoginLinkDev()
        }
    }

    /** oauth token 가져오기 */
    suspend fun getOauthTokenDev(provider: OauthProvider, code: String): OauthTokenResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getOauthTokenDev(code)
        }
    }

    /** 회원가입 가능 여부 체크. */
    @Transactional(readOnly = true)
    suspend fun checkRegisterValid(provider: OauthProvider, accessToken: String): AbleRegisterResponse {
        return when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.checkRegisterValid(accessToken)
        }
    }

    /** 회원가입 */
    @Transactional
    suspend fun register(
        provider: OauthProvider,
        accessToken: String,
        oauthRegisterRequest: OauthRegisterRequest,
    ): TokenDto {
        val oauthInfo = when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getKakaoUserInfo(accessToken)
        }.oauthInfo

        val canRegisterDeferred = userService.existsByOauthInfo(oauthInfo)
        if (canRegisterDeferred) {
            throw NotFoundException(ErrorCode.ALREADY_REGISTERED_USER)
        }

        val user = User.toEntity(oauthRegisterRequest, oauthInfo)
            .run { userService.saveSync(this) }

        val tokenDto = tokenGenerateHelper.generateAccessAndRefreshToken(user.id)

        val refreshToken = RefreshToken(
            id = user.id,
            refreshToken = tokenDto.refreshToken,
            ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
        )

        refreshTokenService.save(refreshToken)

        return tokenDto
    }

    /** 로그인 */
    @Transactional
    suspend fun login(provider: OauthProvider, request: OAuthLoginRequest): TokenDto {
        val oauthInfo = when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.getKakaoUserInfo(request.accessToken)
        }.oauthInfo

        val user = userService.findByOauthInfoOrThrow(oauthInfo)
        val tokenDto = tokenGenerateHelper.generateAccessAndRefreshToken(user.id)

        val refreshToken = RefreshToken(
            id = user.id,
            refreshToken = tokenDto.refreshToken,
            ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
        )

        refreshTokenService.save(refreshToken)

        return tokenDto
    }
}
