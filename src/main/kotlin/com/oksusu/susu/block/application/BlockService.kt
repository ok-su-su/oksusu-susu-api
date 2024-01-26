package com.oksusu.susu.block.application

import com.oksusu.susu.block.domain.Block
import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.block.infrastructure.BlockRepository
import com.oksusu.susu.block.model.UserAndPostBlockIdModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService(
    private val blockRepository: BlockRepository,
) {
    suspend fun validateNotAlreadyBlock(uid: Long, targetId: Long, targetType: BlockTargetType) {
        withContext(Dispatchers.IO) {
            blockRepository.existsByUidAndTargetIdAndTargetType(uid, targetId, targetType)
        }.takeUnless { isExist -> isExist } ?: throw InvalidRequestException(ErrorCode.ALREADY_BLOCKED_TARGET)
    }

    @Transactional
    fun saveSync(block: Block): Block {
        return blockRepository.save(block)
    }

    suspend fun findAllByUid(uid: Long): List<Block> {
        return withContext(Dispatchers.IO) {
            blockRepository.findAllByUid(uid)
        }
    }

    suspend fun getUserAndPostBlockTargetIds(uid: Long): UserAndPostBlockIdModel {
        val blocks = findAllByUid(uid)

        val userBlockIds = blocks.filter { it.targetType == BlockTargetType.USER }
            .map { block -> block.targetId }.toSet()
        val postBlockIds = blocks.filter { it.targetType == BlockTargetType.POST }
            .map { block -> block.targetId }.toSet()

        return UserAndPostBlockIdModel(
            userBlockIds = userBlockIds,
            postBlockIds = postBlockIds
        )
    }

    suspend fun findByIdOrNull(id: Long): Block? {
        return withContext(Dispatchers.IO) {
            blockRepository.findByIdOrNull(id)
        }
    }

    suspend fun findByIdOrThrow(id: Long): Block {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_BLOCK_ERROR)
    }

    suspend fun validateAuthority(uid: Long, id: Long) {
        findByIdOrThrow(id).takeIf { block -> block.uid == uid }
            ?: throw InvalidRequestException(ErrorCode.NOT_BLOCKED_TARGET)
    }

    @Transactional
    fun deleteById(id: Long) {
        blockRepository.deleteById(id)
    }
}
