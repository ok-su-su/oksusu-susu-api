package com.oksusu.susu.batch.user.scheduler

import com.oksusu.susu.batch.user.job.DeleteWithdrawUserDataJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.LoggingCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserScheduler(
    private val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun deleteWithdrawUserData() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            deleteWithdrawUserDataJob.deleteWithdrawUserDataForWeek()
        }
    }
}
