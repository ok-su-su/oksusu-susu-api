package com.oksusu.susu.user.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.user.domain.OauthInfo
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.infrastructure.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    suspend fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean {
        return withContext(Dispatchers.IO) {
            userRepository.existsByOauthInfo(oauthInfo)
        }
    }

    suspend fun findByIdOrThrow(uid: Long): User {
        return findByIdOrNull(uid) ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND_ERROR)
    }

    suspend fun findByIdOrNull(uid: Long): User? {
        return withContext(Dispatchers.IO) { userRepository.findByIdOrNull(uid) }
    }

    fun findByIdOrThrowSync(uid: Long): User {
        return findByIdOrNullSync(uid) ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND_ERROR)
    }

    fun findByIdOrNullSync(uid: Long): User? {
        return userRepository.findByIdOrNull(uid)
    }
}
