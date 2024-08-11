package com.oksusu.susu.batch.user.scheduler

import com.oksusu.susu.batch.user.job.DeleteWithdrawUserDataJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.resolveCancellation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class UserScheduler(
    private val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

//    @Scheduled(cron = "0 0 3 * * *")
    fun deleteWithdrawUserData() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            runCatching {
                deleteWithdrawUserDataJob.deleteWithdrawUserDataForDay()
            }.onFailure { e ->
                logger.resolveCancellation("[BATCH] fail to run deleteWithdrawUserDataForDay", e)
            }
        }
    }
}
