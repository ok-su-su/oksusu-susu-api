package com.oksusu.susu.auth.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.AuthUserImpl
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.TokenDto
import com.oksusu.susu.auth.model.response.TokenRefreshRequest
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidTokenException
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.post.application.PostService
import com.oksusu.susu.user.application.UserService
import com.oksusu.susu.user.application.UserStatusService
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
        val user = userService.findByIdOrThrow(authUser.uid)

        parZip(
            { oauthService.withdraw(user.oauthInfo) },
            { userService.withdraw(authUser.uid) },
            { userStatusService.withdraw(authUser.uid) }
        ) { _, _, _ ->
            val deactivatedPosts = postService.findAllByUid(authUser.uid)
                .map { post ->
                    post.apply {
                        isActive = false
                    }
                }

            postService.saveAllSync(deactivatedPosts)
        }
    }
}
