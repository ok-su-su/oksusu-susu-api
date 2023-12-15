package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.infrastructure.repository.RefreshTokenRedisEntityRepository
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.AuthUserImpl
import com.oksusu.susu.auth.model.AuthUserToken
import com.oksusu.susu.auth.model.RefreshTokenRedisEntity
import com.oksusu.susu.auth.model.dto.TokenDto
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidTokenException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.user.infrastructure.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService,
    val refreshTokenRedisEntityRepository: RefreshTokenRedisEntityRepository,
    private val tokenGenerateHelper: TokenGenerateHelper,

) {
    fun resolveAuthUser(token: Mono<AuthUserToken>): Mono<Any> {
        return jwtTokenService.verifyTokenMono(token)
            .map { payload ->
                if (payload.type != "accessToken") {
                    throw InvalidTokenException(ErrorCode.NOT_ACCESS_TOKEN)
                }
                val user = userRepository.findByIdOrNull(payload.id)
                    ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND_ERROR)

                AuthUserImpl(user.id)
            }
    }

    @Transactional
    suspend fun logout(authUser: AuthUser) = withContext(Dispatchers.IO) {
        async {
            refreshTokenRedisEntityRepository.deleteById(authUser.id)
        }.await()
    }

    @Transactional
    suspend fun refreshToken(authUser: AuthUser, refreshToken: String): TokenDto {
        jwtTokenService.verifyRefreshToken(refreshToken)
        return withContext(Dispatchers.IO) {
            async {
                refreshTokenRedisEntityRepository.deleteById(authUser.id)
            }.await()

            val tokenDto = tokenGenerateHelper.generateAccessAndRefreshToken(authUser.id)

            val refreshTokenRedisEntity = RefreshTokenRedisEntity(
                id = authUser.id,
                refreshToken = tokenDto.refreshToken,
                ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
            )
            async {
                refreshTokenRedisEntityRepository.save(refreshTokenRedisEntity)
            }.await()

            tokenDto
        }
    }
}
