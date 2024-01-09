package com.oksusu.susu.block.application

import com.oksusu.susu.block.domain.Block
import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.block.infrastructure.BlockRepository
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class BlockService(
    private val blockRepository: BlockRepository,
) {
    suspend fun validateNotAlreadyBlock(uid: Long, targetId: Long, targetType: BlockTargetType) {
        withContext(Dispatchers.IO) {
            blockRepository.existsByUidAndTargetIdAndTargetType(uid, targetId, targetType)
        }.takeUnless { it } ?: throw InvalidRequestException(ErrorCode.ALREADY_BLOCKED_TARGET)
    }

    fun saveSync(block: Block): Block {
        return blockRepository.save(block)
    }
}
