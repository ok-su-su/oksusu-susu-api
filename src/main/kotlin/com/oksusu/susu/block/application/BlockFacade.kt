package com.oksusu.susu.block.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.block.domain.Block
import com.oksusu.susu.block.domain.vo.BlockTargetType
import com.oksusu.susu.block.model.request.CreateBlockRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.post.application.PostService
import com.oksusu.susu.user.application.UserService
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
        coroutineScope {
            val validateNotBlock = async {
                blockService.validateNotAlreadyBlock(user.id, request.targetId, request.targetType)
            }
            val validateTargetExist = when (request.targetType) {
                BlockTargetType.POST -> async { postService.validateExist(request.targetId) }
                BlockTargetType.USER -> async { userService.validateExist(request.targetId) }
            }

            awaitAll(validateNotBlock, validateTargetExist)
        }

        txTemplates.writer.coExecute {
            Block(
                uid = user.id,
                targetId = request.targetId,
                targetType = request.targetType,
                reason = request.reason
            ).run { blockService.saveSync(this) }
        }
    }
}
