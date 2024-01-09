package com.oksusu.susu.user.application

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.user.domain.OauthInfo
import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.domain.vo.UserState
import com.oksusu.susu.user.infrastructure.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun validateNotRegistered(oauthInfo: OauthInfo) {
        existsByOauthInfo(oauthInfo).takeUnless { isExists -> isExists }
            ?: throw NotFoundException(ErrorCode.ALREADY_REGISTERED_USER)
    }

    suspend fun existsByOauthInfo(oauthInfo: OauthInfo): Boolean {
        return withContext(Dispatchers.IO) { userRepository.existsByOauthInfo(oauthInfo) }
    }

    @Transactional
    fun saveSync(user: User): User {
        return userRepository.save(user)
    }

    suspend fun findByOauthInfoOrThrow(oauthInfo: OauthInfo): User {
        return findByOauthInfoOrNull(oauthInfo) ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND_ERROR)
    }

    suspend fun findByOauthInfoOrNull(oauthInfo: OauthInfo): User? {
        return withContext(Dispatchers.IO) { userRepository.findByOauthInfo(oauthInfo) }
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

    suspend fun withdraw(uid: Long) {
        val user = findByIdOrThrow(uid)

        txTemplates.writer.coExecute {
            user.apply {
                this.userState = UserState.DELETED
                this.oauthInfo = oauthInfo.withdrawOauthInfo()
            }.run { saveSync(this) }
        }
    }

    suspend fun validateExist(id: Long) {
        withContext(Dispatchers.IO) {
            userRepository.existsById(id)
        }.takeIf { it } ?: throw InvalidRequestException(ErrorCode.USER_NOT_FOUND_ERROR)
    }
}
