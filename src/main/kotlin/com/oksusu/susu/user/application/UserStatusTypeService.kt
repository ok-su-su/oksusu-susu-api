package com.oksusu.susu.user.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.extension.withJob
import com.oksusu.susu.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.user.infrastructure.UserStatusTypeRepository
import com.oksusu.susu.user.model.UserStatusTypeModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class UserStatusTypeService(
    private val userStatusTypeRepository: UserStatusTypeRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var statuses: Map<Long, UserStatusTypeModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-statuses.initial-delay:0}"
    )
    fun refreshStatuses() {
        CoroutineScope(Dispatchers.IO.withJob()).launch {
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
