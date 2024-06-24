package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.CreateUserWithdrawEvent
import com.oksusu.susu.api.user.application.UserWithdrawService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.transaction.event.TransactionalEventListener

@SusuEventListener
class UserWithdrawEventListener(
    private val userWithdrawService: UserWithdrawService,
) {
    private val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createUserWithdrawService(event: CreateUserWithdrawEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            val userWithdraw = event.userWithdraw

            logger.info { "[${event.publishAt}] ${userWithdraw.uid} 유저 탈퇴 엔티티 저장 시작" }

            userWithdrawService.saveSync(userWithdraw)

            logger.info { "[${event.publishAt}] ${userWithdraw.uid} 유저 탈퇴 엔티티 저장 끝" }
        }
    }
}
