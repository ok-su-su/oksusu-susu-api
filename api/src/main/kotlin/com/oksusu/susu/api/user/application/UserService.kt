package com.oksusu.susu.api.user.application

import com.oksusu.susu.common.exception.AlreadyException
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import com.oksusu.susu.domain.user.infrastructure.model.UserAndUserStatusModel
import kotlinx.coroutines.Dispatchers
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun validateNotRegistered(oauthInfo: OauthInfo) {
        if (existsByOAuthInfo(oauthInfo)) {
            /** 중복 가입에 대한 Logging을 확인하기 위한 용도 */
            val reason: Map<String, Any> = mapOf(
                "oauthProvider" to oauthInfo.oAuthProvider.name,
                "oauthId" to oauthInfo.oAuthId
            )

            throw AlreadyException(ErrorCode.ALREADY_REGISTERED_USER, reason)
        }
    }

    suspend fun existsByOAuthInfo(oauthInfo: OauthInfo): Boolean {
        return withMDCContext(Dispatchers.IO) { userRepository.existsByOauthInfo(oauthInfo) }
    }

    @Transactional
    fun saveSync(user: User): User {
        return userRepository.save(user)
    }

    suspend fun findByOAuthInfoOrThrow(oauthInfo: OauthInfo): User {
        return findByOAuthInfoOrNull(oauthInfo) ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_ERROR)
    }

    suspend fun findByOAuthInfoOrNull(oauthInfo: OauthInfo): User? {
        return withMDCContext(Dispatchers.IO) { userRepository.findByOauthInfo(oauthInfo) }
    }

    suspend fun findByIdOrThrow(uid: Long): User {
        return findByIdOrNull(uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_ERROR)
    }

    suspend fun findByIdOrNull(uid: Long): User? {
        return withMDCContext(Dispatchers.IO) { userRepository.findByIdOrNull(uid) }
    }

    fun findByIdOrThrowSync(uid: Long): User {
        return findByIdOrNullSync(uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_ERROR)
    }

    fun findByIdOrNullSync(uid: Long): User? {
        return userRepository.findByIdOrNull(uid)
    }

    suspend fun validateExist(id: Long) {
        withMDCContext(Dispatchers.IO) {
            userRepository.existsById(id)
        }.takeIf { it } ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_USER_ERROR)
    }

    suspend fun existsById(id: Long): Boolean {
        return withMDCContext(Dispatchers.IO) { userRepository.existsById(id) }
    }

    suspend fun countByCreatedAtBetween(
        startAt: LocalDateTime,
        endAt: LocalDateTime,
    ): Long {
        return withMDCContext(Dispatchers.IO) { userRepository.countByCreatedAtBetween(startAt, endAt) }
    }

    suspend fun count() {
        return withMDCContext(Dispatchers.IO) { userRepository.count() }
    }

    suspend fun getUserAndUserStatus(uid: Long): UserAndUserStatusModel {
        return withMDCContext(Dispatchers.IO) {
            getUserAndUserStatusSync(uid)
        }
    }

    fun getUserAndUserStatusSync(uid: Long): UserAndUserStatusModel {
        return userRepository.getUserAndUserStatus(uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_ERROR)
    }
}
