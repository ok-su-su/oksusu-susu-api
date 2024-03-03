package com.oksusu.susu.event.listener

import com.oksusu.susu.event.model.CreateUserStatusHistoryEvent
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.user.infrastructure.UserStatusHistoryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserStatusHistoryEventListener(
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createUserStatusHistoryService(event: CreateUserStatusHistoryEvent) {
        CoroutineScope(Dispatchers.IO.withMDCContext()).launch {
            val history = event.userStatusHistory

            logger.info { "[${event.publishAt}] ${history.uid} 유저 user status ${history.fromStatusId} -> ${history.toStatusId} 변경 시작" }

            userStatusHistoryRepository.save(history)

            logger.info { "[${event.publishAt}] ${history.uid} 유저 user status ${history.fromStatusId} -> ${history.toStatusId} 변경 끝" }
        }
    }
}
