package com.oksusu.susu.auth.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.AuthUserImpl
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.TokenDto
import com.oksusu.susu.auth.model.response.TokenRefreshRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.event.model.CreateUserStatusHistoryEvent
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidTokenException
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.post.application.PostService
import com.oksusu.susu.user.application.UserStatusTypeService
import com.oksusu.susu.user.application.UserService
import com.oksusu.susu.user.application.UserStatusService
import com.oksusu.susu.user.domain.UserStatusHistory
import com.oksusu.susu.user.domain.vo.UserStatusAssignmentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class AuthFacade(
    private val userService: UserService,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenService: RefreshTokenService,
    private val tokenGenerateHelper: TokenGenerateHelper,
    private val oauthService: OAuthService,
    private val postService: PostService,
    private val userStatusService: UserStatusService,
    private val eventPublisher: ApplicationEventPublisher,
    private val txTemplates: TransactionTemplates,
    private val userStatusTypeService: UserStatusTypeService,
) {
    fun resolveAuthUser(token: Mono<AuthUserToken>): Mono<Any> {
        return jwtTokenService.verifyTokenMono(token)
            .map { payload ->
                if (payload.type != "accessToken") {
                    throw InvalidTokenException(ErrorCode.INVALID_ACCESS_TOKEN)
                }
                val user = userService.findByIdOrThrowSync(payload.id)

                AuthUserImpl(user.id)
            }
    }

    @Transactional
    suspend fun logout(user: AuthUser) {
        refreshTokenService.deleteByKey(user.uid.toString())
    }

    @Transactional
    suspend fun refreshToken(request: TokenRefreshRequest): TokenDto {
        val accessPayload = jwtTokenService.verifyTokenWithExtendedExpiredAt(request.accessToken)
        val refreshPayload = jwtTokenService.verifyRefreshToken(request.refreshToken)

        if (accessPayload.id != refreshPayload.id) {
            throw NoAuthorityException(ErrorCode.INVALID_TOKEN)
        }

        return parZip(
            { refreshTokenService.deleteByKey(refreshPayload.id.toString()) },
            { tokenGenerateHelper.generateAccessAndRefreshToken(refreshPayload.id) }
        ) { _, tokenDto ->
            RefreshToken(
                uid = refreshPayload.id,
                refreshToken = tokenDto.refreshToken
            ).run { refreshTokenService.save(this) }

            tokenDto
        }
    }

    @Transactional
    suspend fun withdraw(authUser: AuthUser) {
        val (deactivatedPosts, userAndUserStatusModel) = parZip(
            { postService.findAllByUid(authUser.uid) },
            { userService.getUserAndUserStatus(authUser.uid) }
        ) { posts, userAndUserStatusModel ->
            val deactivatedPosts = posts.map { post -> post.apply { isActive = false } }

            deactivatedPosts to userAndUserStatusModel
        }

        val user = userAndUserStatusModel.user
        val userStatus = userAndUserStatusModel.userStatus

        coroutineScope {
            val txDeferred = async {
                txTemplates.writer.coExecuteOrNull {
                    user.apply {
                        this.oauthInfo = oauthInfo.withdrawOauthInfo()
                    }.run { userService.saveSync(this) }

                    postService.saveAllSync(deactivatedPosts)

                    eventPublisher.publishEvent(
                        CreateUserStatusHistoryEvent(
                            userStatusHistory = UserStatusHistory(
                                uid = user.id,
                                statusAssignmentType = UserStatusAssignmentType.ACCOUNT,
                                fromStatusId = userStatus.accountStatusId,
                                toStatusId = userStatusTypeService.getDeletedStatusId()
                            )
                        )
                    )

                    userStatus.apply {
                        accountStatusId = userStatusTypeService.getDeletedStatusId()
                    }.run { userStatusService.saveSync(this) }
                }
            }

            val oauthDeferred = async {
                oauthService.withdraw(user.oauthInfo)
            }

            awaitAll(txDeferred, oauthDeferred)
        }
    }
}
