package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.auth.model.dto.request.OAuthLoginRequest
import com.oksusu.susu.auth.model.dto.request.OauthRegisterRequest
import com.oksusu.susu.auth.model.dto.response.AbleRegisterResponse
import com.oksusu.susu.auth.model.dto.response.UserOAuthInfoResponse
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.event.model.TermAgreementHistoryCreateEvent
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.term.application.TermAgreementService
import com.oksusu.susu.term.application.TermService
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType
import com.oksusu.susu.user.application.UserService
import com.oksusu.susu.user.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.context.ApplicationEventPublisher
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
    private val eventPublisher: ApplicationEventPublisher,
) {
    /** 회원가입 가능 여부 체크. */
    suspend fun checkRegisterValid(provider: OauthProvider, accessToken: String): AbleRegisterResponse {
        val oauthInfo = oAuthService.getOauthUserInfo(provider, accessToken)

        val isExistUser = userService.existsByOauthInfo(oauthInfo)

        return AbleRegisterResponse(!isExistUser)
    }

    /** 회원가입 */
    suspend fun register(
        provider: OauthProvider,
        accessToken: String,
        request: OauthRegisterRequest,
    ): TokenDto {
        val oauthInfo = oAuthService.getOauthUserInfo(provider, accessToken)

        coroutineScope {
            val validateNotRegistered = async(Dispatchers.IO) { userService.validateNotRegistered(oauthInfo) }
            val validateExistTerms = async(Dispatchers.IO) { termService.validateExistTerms(request.termAgreement) }

            validateNotRegistered.await()
            validateExistTerms.await()
        }

        val user = txTemplates.writer.coExecute {
            val createdUser = User.toEntity(request, oauthInfo)
                .run { userService.saveSync(this) }

            val termAgreements = request.termAgreement
                .map { TermAgreement(uid = createdUser.id, termId = it) }
                .run { termAgreementService.saveAllSync(this) }

            eventPublisher.publishEvent(
                TermAgreementHistoryCreateEvent(
                    termAgreements = termAgreements,
                    changeType = TermAgreementChangeType.AGREEMENT
                )
            )

            createdUser
        }

        return generateTokenDto(user.id)
    }

    /** 로그인 */
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
    suspend fun loginWithCode(
        provider: OauthProvider,
        code: String,
        request: AbstractServerHttpRequest,
    ): String {
        val oauthToken = oAuthService.getOauthToken(provider, code, request)

        return this.login(OauthProvider.KAKAO, OAuthLoginRequest(oauthToken.accessToken)).accessToken
    }

    suspend fun getOAuthInfo(user: AuthUser): UserOAuthInfoResponse {
        return userService.findByIdOrThrow(user.id).run { UserOAuthInfoResponse.from(this) }
    }
}
