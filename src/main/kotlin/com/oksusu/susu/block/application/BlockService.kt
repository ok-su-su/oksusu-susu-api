package com.oksusu.susu.block.application

import com.oksusu.susu.block.domain.Block
import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.block.infrastructure.BlockRepository
import com.oksusu.susu.block.model.UserAndPostBlockIdModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            .map { block -> block.targetId }
        val postBlockIds = blocks.filter { it.targetType == BlockTargetType.POST }
            .map { block -> block.targetId }

        return UserAndPostBlockIdModel(
            userBlockIds = userBlockIds,
            postBlockIds = postBlockIds
        )
    }
}
