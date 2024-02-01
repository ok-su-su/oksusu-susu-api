package com.oksusu.susu.user.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.user.domain.vo.StatusType
import com.oksusu.susu.user.infrastructure.StatusRepository
import com.oksusu.susu.user.model.StatusModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled

class StatusService(
    private val statusRepository: StatusRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var statuses: Map<Long, StatusModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-statuses.initial-delay:0}"
    )
    fun refreshStatuses() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh statuses" }

            statuses = runCatching {
                statusRepository.findAllByIsActive(true)
                    .map { status -> StatusModel.from(status) }
                    .associateBy { statuses -> statuses.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshBoards", e)
            }.getOrDefault(statuses)

            logger.info { "finish refresh statuses" }
        }
    }

    fun getStatus(id: Long): StatusModel {
        return statuses[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_STATUS_ERROR)
    }

    fun getActiveStatus(): StatusModel {
        return statuses.values.firstOrNull { status -> status.statusType == StatusType.ACTIVE }
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_STATUS_ERROR)
    }

    fun getDeletedStatus(): StatusModel {
        return statuses.values.firstOrNull { status -> status.statusType == StatusType.DELETED }
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_STATUS_ERROR)
    }
}
