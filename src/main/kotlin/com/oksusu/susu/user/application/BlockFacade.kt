package com.oksusu.susu.user.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.post.application.PostService
import com.oksusu.susu.user.domain.UserBlock
import com.oksusu.susu.user.domain.vo.UserBlockTargetType
import com.oksusu.susu.user.model.request.CreateBlockRequest
import com.oksusu.susu.user.model.request.DeleteBlockRequest
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class BlockFacade(
    private val blockService: BlockService,
    private val postService: PostService,
    private val userService: UserService,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun createBlock(user: AuthUser, request: CreateBlockRequest) {
        if (request.targetType == UserBlockTargetType.USER && user.isAuthor(request.targetId)) {
            throw InvalidRequestException(ErrorCode.CANNOT_BLOCK_MYSELF)
        }

        coroutineScope {
            val validateNotBlock = async {
                blockService.validateNotAlreadyBlock(user.uid, request.targetId, request.targetType)
            }
            val validateTargetExist = when (request.targetType) {
                UserBlockTargetType.POST -> async { postService.validateExist(request.targetId) }
                UserBlockTargetType.USER -> async { userService.validateExist(request.targetId) }
            }

            awaitAll(validateNotBlock, validateTargetExist)
        }

        txTemplates.writer.coExecute {
            UserBlock(
                uid = user.uid,
                targetId = request.targetId,
                targetType = request.targetType,
                reason = request.reason
            ).run { blockService.saveSync(this) }
        }
    }

    suspend fun deleteBlock(user: AuthUser, id: Long) {
        blockService.validateAuthority(user.uid, id)

        txTemplates.writer.coExecute {
            blockService.deleteById(id)
        }
    }

    suspend fun deleteBlockByTargetId(user: AuthUser, request: DeleteBlockRequest) {
        val block = blockService.findByTargetIdAndTargetType(request.targetId, request.targetType)

        user.isNotAuthorThrow(block.uid)

        txTemplates.writer.coExecute {
            blockService.deleteById(block.id)
        }
    }
}
