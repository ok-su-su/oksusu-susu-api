package com.oksusu.susu.auth.application

import com.oksusu.susu.auth.helper.KakaoOauthHelper
import com.oksusu.susu.auth.helper.TokenGenerateHelper
import com.oksusu.susu.auth.infrastructure.repository.RefreshTokenRedisEntityRepository
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.RefreshTokenRedisEntity
import com.oksusu.susu.auth.model.dto.*
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.infrastructure.UserRepository
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OauthService(
    private val kakaoOauthHelper: KakaoOauthHelper,
    private val tokenGenerateHelper: TokenGenerateHelper,
    val userRepository: UserRepository,
    val refreshTokenRedisEntityRepository: RefreshTokenRedisEntityRepository,
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
    @Transactional(readOnly = true)
    suspend fun checkRegisterValid(provider: OauthProvider, accessToken: String): AbleRegisterResponse =
        when (provider) {
            OauthProvider.KAKAO -> kakaoOauthHelper.checkRegisterValid(accessToken)
        }

    /** 회원가입 */
    @Transactional
    suspend fun register(
        provider: OauthProvider,
        accessToken: String,
        oauthRegisterRequest: OauthRegisterRequest,
    ): TokenDto =
        withContext(Dispatchers.IO) {
            val oauthInfo = async {
                when (provider) {
                    OauthProvider.KAKAO -> kakaoOauthHelper.getKakaoUserInfo(accessToken)
                }
            }.await().oauthInfo

            val canRegisterDeferred = async {
                userRepository.existsByOauthInfo(oauthInfo)
            }
            if (canRegisterDeferred.await()) {
                throw NotFoundException(ErrorCode.ALREADY_REGISTERED_USER)
            }

            val user = async {
                userRepository.save(User.toEntity(oauthRegisterRequest, oauthInfo))
            }.await()
            val tokenDto = tokenGenerateHelper.generateAccessAndRefreshToken(user.id)

            val refreshTokenRedisEntity = RefreshTokenRedisEntity(
                id = user.id,
                refreshToken = tokenDto.refreshToken,
                ttl = tokenGenerateHelper.getRefreshTokenTtlSecond()
            )
            async {
                refreshTokenRedisEntityRepository.save(refreshTokenRedisEntity)
            }.await()

            tokenDto
        }
}
