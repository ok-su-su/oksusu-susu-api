package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.auth.model.dto.request.OAuthLoginRequest
import com.oksusu.susu.auth.model.dto.request.OauthRegisterRequest
import com.oksusu.susu.auth.model.dto.response.AbleRegisterResponse
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.term.application.TermAgreementService
import com.oksusu.susu.term.application.TermService
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.user.application.UserService
import com.oksusu.susu.user.domain.User
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuthFacade(
    private val userService: UserService,
    private val refreshTokenService: RefreshTokenService,
    private val tokenGenerateHelper: TokenGenerateHelper,
    private val oAuthService: OAuthService,
    private val txTemplates: TransactionTemplates,
    private val termService: TermService,
    private val termAgreementService: TermAgreementService,
) {
    /** 회원가입 가능 여부 체크. */
    @Transactional(readOnly = true)
    suspend fun checkRegisterValid(provider: OauthProvider, accessToken: String): AbleRegisterResponse {
        val oauthInfo = oAuthService.getOauthUserInfo(provider, accessToken)

        val isExistUser = userService.existsByOauthInfo(oauthInfo)

        return AbleRegisterResponse(!isExistUser)
    }

    /** 회원가입 */
    @Transactional
    suspend fun register(
        provider: OauthProvider,
        accessToken: String,
        request: OauthRegisterRequest,
    ): TokenDto {
        val oauthInfo = oAuthService.getOauthUserInfo(provider, accessToken)

        userService.validateNotRegistered(oauthInfo)
        termService.validateExistTerms(request.termAgreement)

        val user = txTemplates.writer.executeWithContext {
            val createdUser = User.toEntity(request, oauthInfo)
                .run { userService.saveSync(this) }

            request.termAgreement.map { TermAgreement(uid = createdUser.id, termId = it) }
                .run { termAgreementService.saveAllSync(this) }

            createdUser
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_USER_ERROR)

        return generateTokenDto(user.id)
    }

    /** 로그인 */
    @Transactional
    suspend fun login(provider: OauthProvider, request: OAuthLoginRequest): TokenDto {
        val oauthInfo = oAuthService.getOauthUserInfo(provider, request.accessToken)

        val user = userService.findByOauthInfoOrThrow(oauthInfo)

        return generateTokenDto(user.id)
    }

    private suspend fun generateTokenDto(uid: Long): TokenDto {
        val tokenDto = tokenGenerateHelper.generateAccessAndRefreshToken(uid)

        val refreshToken = RefreshToken(
            id = uid,
            refreshToken = tokenDto.refreshToken,
            ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
        )

        refreshTokenService.saveSync(refreshToken)

        return tokenDto
    }

    /** 회원탈퇴 callback 페이지용 oauth 토큰 받아오기 + susu token 발급해주기 */
    @Transactional
    suspend fun loginWithCode(
        provider: OauthProvider,
        code: String,
        request: AbstractServerHttpRequest,
    ): String {
        val oauthToken = oAuthService.getOauthToken(provider, code, request)

        return this.login(OauthProvider.KAKAO, OAuthLoginRequest(oauthToken.accessToken)).accessToken
    }
}
