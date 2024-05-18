package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.CreateUserStatusHistoryEvent
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class UserStatusHistoryEventListener(
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createUserStatusHistoryService(event: CreateUserStatusHistoryEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            val history = event.userStatusHistory

            logger.info { "[${event.publishAt}] ${history.uid} 유저 user status ${history.fromStatusId} -> ${history.toStatusId} 변경 시작" }

            userStatusHistoryRepository.save(history)

            logger.info { "[${event.publishAt}] ${history.uid} 유저 user status ${history.fromStatusId} -> ${history.toStatusId} 변경 끝" }
        }
    }
}
