package com.oksusu.susu.api.auth.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.auth.model.TokenDto
import com.oksusu.susu.api.auth.model.request.OAuthLoginRequest
import com.oksusu.susu.api.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.api.auth.model.response.AbleRegisterResponse
import com.oksusu.susu.api.auth.model.response.UserOAuthInfoResponse
import com.oksusu.susu.api.event.model.CreateUserDeviceEvent
import com.oksusu.susu.api.event.model.CreateUserStatusHistoryEvent
import com.oksusu.susu.api.event.model.TermAgreementHistoryCreateEvent
import com.oksusu.susu.api.event.model.UpdateUserDeviceEvent
import com.oksusu.susu.api.term.application.TermAgreementService
import com.oksusu.susu.api.term.application.TermService
import com.oksusu.susu.api.user.application.UserService
import com.oksusu.susu.api.user.application.UserStatusService
import com.oksusu.susu.api.user.application.UserStatusTypeService
import com.oksusu.susu.api.user.model.UserDeviceContext
import com.oksusu.susu.api.user.model.UserDeviceContextImpl
import com.oksusu.susu.domain.auth.domain.RefreshToken
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.term.domain.TermAgreement
import com.oksusu.susu.domain.term.domain.vo.TermAgreementChangeType
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.UserDevice
import com.oksusu.susu.domain.user.domain.UserStatus
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service

@Service
class OAuthFacade(
    private val userService: UserService,
    private val refreshTokenService: RefreshTokenService,
    private val oAuthService: OAuthService,
    private val txTemplates: TransactionTemplates,
    private val termService: TermService,
    private val termAgreementService: TermAgreementService,
    private val eventPublisher: ApplicationEventPublisher,
    private val userStatusService: UserStatusService,
    private val userStatusTypeService: UserStatusTypeService,
    private val authValidateService: AuthValidateService,
    private val jwtTokenService: JwtTokenService,
) {
    val logger = KotlinLogging.logger {}

    /** 회원가입 가능 여부 체크. */
    suspend fun checkRegisterValid(provider: OAuthProvider, accessToken: String): AbleRegisterResponse {
        val oauthInfo = oAuthService.getOAuthInfo(provider, accessToken)

        val isExistUser = userService.existsByOAuthInfo(oauthInfo)

        return AbleRegisterResponse(!isExistUser)
    }

    /** 회원가입 */
    suspend fun register(
        provider: OAuthProvider,
        accessToken: String,
        request: OAuthRegisterRequest,
        deviceContext: UserDeviceContext,
    ): TokenDto {
        authValidateService.validateRegisterRequest(request)

        val oauthInfo = oAuthService.getOAuthInfo(provider, accessToken)

        coroutineScope {
            val validateNotRegistered = async(Dispatchers.IO) {
                userService.validateNotRegistered(
                    oauthInfo
                )
            }
            val validateExistTerms = async(Dispatchers.IO) {
                termService.validateExistTerms(
                    request.termAgreement
                )
            }

            awaitAll(validateNotRegistered, validateExistTerms)
        }

        val user = txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
            val createdUser = User(
                oauthInfo = oauthInfo,
                name = request.name,
                gender = request.gender,
                birth = request.getBirth(),
                role = AccountRole.USER
            ).run { userService.saveSync(this) }

            UserStatus(
                uid = createdUser.id,
                accountStatusId = userStatusTypeService.getActiveStatusId(),
                communityStatusId = userStatusTypeService.getActiveStatusId()
            ).run { userStatusService.saveSync(this) }

            val termAgreements = request.termAgreement
                .map { TermAgreement(uid = createdUser.id, termId = it) }
                .run { termAgreementService.saveAllSync(this) }

            val userDevice = UserDevice(
                uid = createdUser.id,
                applicationVersion = deviceContext.applicationVersion,
                deviceId = deviceContext.deviceId,
                deviceSoftwareVersion = deviceContext.deviceSoftwareVersion,
                lineNumber = deviceContext.lineNumber,
                networkCountryIso = deviceContext.networkCountryIso,
                networkOperator = deviceContext.networkOperator,
                networkOperatorName = deviceContext.networkOperatorName,
                networkType = deviceContext.networkType,
                phoneType = deviceContext.phoneType,
                simSerialNumber = deviceContext.simSerialNumber,
                simState = deviceContext.simState
            )

            eventPublisher.publishEvent(
                TermAgreementHistoryCreateEvent(
                    termAgreements = termAgreements,
                    changeType = TermAgreementChangeType.AGREEMENT
                )
            )
            eventPublisher.publishEvent(
                CreateUserDeviceEvent(userDevice)
            )
            eventPublisher.publishEvent(
                CreateUserStatusHistoryEvent(
                    userStatusHistory = UserStatusHistory(
                        uid = createdUser.id,
                        statusAssignmentType = UserStatusAssignmentType.ACCOUNT,
                        fromStatusId = userStatusTypeService.getActiveStatusId(),
                        toStatusId = userStatusTypeService.getActiveStatusId()
                    )
                )
            )

            createdUser
        }

        return generateTokenDto(user.id)
    }

    /** 로그인 */
    suspend fun login(
        provider: OAuthProvider,
        request: OAuthLoginRequest,
        deviceContext: UserDeviceContext,
    ): TokenDto {
        val oauthInfo = oAuthService.getOAuthInfo(provider, request.accessToken)
        val user = userService.findByOAuthInfoOrThrow(oauthInfo)

        val userDevice = UserDevice(
            uid = user.id,
            applicationVersion = deviceContext.applicationVersion,
            deviceId = deviceContext.deviceId,
            deviceSoftwareVersion = deviceContext.deviceSoftwareVersion,
            lineNumber = deviceContext.lineNumber,
            networkCountryIso = deviceContext.networkCountryIso,
            networkOperator = deviceContext.networkOperator,
            networkOperatorName = deviceContext.networkOperatorName,
            networkType = deviceContext.networkType,
            phoneType = deviceContext.phoneType,
            simSerialNumber = deviceContext.simSerialNumber,
            simState = deviceContext.simState
        )

        txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
            eventPublisher.publishEvent(UpdateUserDeviceEvent(userDevice))
        }

        return generateTokenDto(user.id)
    }

    private suspend fun generateTokenDto(uid: Long): TokenDto {
        val tokenDto = jwtTokenService.generateAccessAndRefreshToken(uid)

        val refreshToken = RefreshToken(
            uid = uid,
            refreshToken = tokenDto.refreshToken
        )

        refreshTokenService.save(refreshToken)

        return tokenDto
    }

    /** 회원탈퇴 callback 페이지용 oauth 토큰 받아오기 + susu token 발급해주기 */
    suspend fun loginWithCodeInWithdraw(
        provider: OAuthProvider,
        code: String,
        request: ServerHttpRequest,
    ): String {
        val oAuthToken = oAuthService.getOAuthWithdrawToken(provider, code)

        return this.login(
            OAuthProvider.KAKAO,
            OAuthLoginRequest(oAuthToken.accessToken),
            UserDeviceContextImpl.getDefault()
        ).accessToken
    }

    suspend fun getOAuthInfo(user: AuthUser): UserOAuthInfoResponse {
        return userService.findByIdOrThrow(user.uid).run { UserOAuthInfoResponse.from(this) }
    }
}
