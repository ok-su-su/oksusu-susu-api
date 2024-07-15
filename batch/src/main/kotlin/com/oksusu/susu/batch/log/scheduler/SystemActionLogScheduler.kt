package com.oksusu.susu.batch.log.scheduler

import com.oksusu.susu.batch.log.job.SystemActionLogDeleteJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SystemActionLogScheduler(
    private val systemActionLogDeleteJob: SystemActionLogDeleteJob,
    private val errorPublishingCoroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    /** 한달이 지난 system action log 삭제 처리 */
    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.delete-system-action-log.initial-delay:100}"
    )
    fun runDeleteJob() {
        CoroutineScope(Dispatchers.IO + errorPublishingCoroutineExceptionHandler.handler).launch {
            systemActionLogDeleteJob.runDeleteJob()
        }
    }
}
