package com.oksusu.susu.user.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.user.domain.UserBlock
import com.oksusu.susu.user.domain.vo.UserBlockTargetType
import com.oksusu.susu.user.infrastructure.UserBlockRepository
import com.oksusu.susu.user.model.UserAndPostBlockIdModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService(
    private val userBlockRepository: UserBlockRepository,
) {
    suspend fun validateNotAlreadyBlock(uid: Long, targetId: Long, targetType: UserBlockTargetType) {
        withContext(Dispatchers.IO.withMDCContext()) {
            userBlockRepository.existsByUidAndTargetIdAndTargetType(uid, targetId, targetType)
        }.takeUnless { isExist -> isExist } ?: throw InvalidRequestException(ErrorCode.ALREADY_BLOCKED_TARGET)
    }

    @Transactional
    fun saveSync(userBlock: UserBlock): UserBlock {
        return userBlockRepository.save(userBlock)
    }

    suspend fun findAllByUid(uid: Long): List<UserBlock> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            userBlockRepository.findAllByUid(uid)
        }
    }

    suspend fun getUserAndPostBlockTargetIds(uid: Long): UserAndPostBlockIdModel {
        val blocks = findAllByUid(uid)

        val userBlockIds = blocks.filter { it.targetType == UserBlockTargetType.USER }
            .map { block -> block.targetId }.toSet()
        val postBlockIds = blocks.filter { it.targetType == UserBlockTargetType.POST }
            .map { block -> block.targetId }.toSet()

        return UserAndPostBlockIdModel(
            userBlockIds = userBlockIds,
            postBlockIds = postBlockIds
        )
    }

    suspend fun findByIdOrNull(id: Long): UserBlock? {
        return withContext(Dispatchers.IO.withMDCContext()) {
            userBlockRepository.findByIdOrNull(id)
        }
    }

    suspend fun findByIdOrThrow(id: Long): UserBlock {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_BLOCK_ERROR)
    }

    suspend fun validateAuthority(uid: Long, id: Long) {
        findByIdOrThrow(id).takeIf { block -> block.uid == uid }
            ?: throw InvalidRequestException(ErrorCode.NOT_BLOCKED_TARGET)
    }

    @Transactional
    fun deleteById(id: Long) {
        userBlockRepository.deleteById(id)
    }

    suspend fun findByTargetIdAndTargetType(targetId: Long, targetType: UserBlockTargetType): UserBlock {
        return withContext(Dispatchers.IO.withMDCContext()) {
            userBlockRepository.findByTargetIdAndTargetType(targetId, targetType)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_BLOCK_ERROR)
    }
}
