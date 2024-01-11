package com.oksusu.susu.auth.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.domain.RefreshToken
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.AuthUserImpl
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.auth.model.dto.response.TokenRefreshRequest
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidTokenException
import com.oksusu.susu.exception.NoAuthorityException
import com.oksusu.susu.user.application.UserService
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
    suspend fun logout(authUser: AuthUser) {
        refreshTokenService.deleteByIdSync(authUser.id)
    }

    @Transactional
    suspend fun refreshToken(request: TokenRefreshRequest): TokenDto {
        val accessPayload = jwtTokenService.verifyTokenWithExtendedExpiredAt(request.accessToken)
        val refreshPayload = jwtTokenService.verifyRefreshToken(request.refreshToken)

        if (accessPayload.id != refreshPayload.id) {
            throw NoAuthorityException(ErrorCode.INVALID_TOKEN)
        }

        return parZip(
            { refreshTokenService.deleteByIdSync(refreshPayload.id) },
            { tokenGenerateHelper.generateAccessAndRefreshToken(refreshPayload.id) }
        ) { _, tokenDto ->

            // TODO : 여기서 tx 열어서 진행, 위에 어노테이션 제거
            RefreshToken(
                id = refreshPayload.id,
                refreshToken = tokenDto.refreshToken,
                ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
            ).run { refreshTokenService.saveSync(this) }

            tokenDto
        }
    }

    // TODO : parzip은 반환값을 이용하는 경우에 사용
    @Transactional
    suspend fun withdraw(authUser: AuthUser) {
        val user = userService.findByIdOrThrow(authUser.id)

        parZip(
            { oauthService.withdraw(user.oauthInfo) },
            { userService.withdraw(authUser.id) }
        ) { _, _ ->
            /** 현재는 추가적인 로직은 없음 */
        }
    }
}
