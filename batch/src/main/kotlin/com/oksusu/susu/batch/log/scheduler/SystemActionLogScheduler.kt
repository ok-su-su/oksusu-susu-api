package com.oksusu.susu.batch.log.scheduler

import com.oksusu.susu.batch.log.job.SystemActionLogDeleteJob
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.resolveCancellation
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SystemActionLogScheduler(
    private val systemActionLogDeleteJob: SystemActionLogDeleteJob,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }

    /** 한달이 지난 system action log 삭제 처리 */
    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.delete-system-action-log.initial-delay:100}"
    )
    fun runDeleteJob() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            runCatching {
                systemActionLogDeleteJob.runDeleteJob()
            }.onFailure { e ->
                logger.resolveCancellation("[BATCH] fail to run runDeleteJob", e)
            }
        }
    }
}
