package com.oksusu.susu.api.user.application

import com.oksusu.susu.api.user.model.UserStatusTypeModel
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.resolveCancellation
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserStatusTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class UserStatusTypeService(
    private val userStatusTypeRepository: UserStatusTypeRepository,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }
    private var statuses: Map<Long, UserStatusTypeModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-statuses.initial-delay:0}"
    )
    fun refreshStatuses() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            logger.info { "start refresh statuses" }

            statuses = runCatching {
                userStatusTypeRepository.findAllByIsActive(true)
                    .map { status -> UserStatusTypeModel.from(status) }
                    .associateBy { statuses -> statuses.id }
            }.onFailure { e ->
                logger.resolveCancellation("refresh statuses", e)
            }.getOrDefault(statuses)

            logger.info { "finish refresh statuses" }
        }
    }

    fun getStatus(id: Long): UserStatusTypeModel {
        return statuses[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_STATUS_TYPE_ERROR)
    }

    fun getActiveStatusId(): Long {
        return statuses.values.firstOrNull { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }?.id
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_STATUS_TYPE_ERROR)
    }

    fun getDeletedStatusId(): Long {
        return statuses.values.firstOrNull { status -> status.statusTypeInfo == UserStatusTypeInfo.DELETED }?.id
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_STATUS_TYPE_ERROR)
    }
}
