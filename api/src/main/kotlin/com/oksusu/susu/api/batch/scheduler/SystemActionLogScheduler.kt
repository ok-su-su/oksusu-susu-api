package com.oksusu.susu.api.batch.scheduler

import com.oksusu.susu.api.batch.job.SystemActionLogDeleteJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SystemActionLogScheduler(
    private val systemActionLogDeleteJob: SystemActionLogDeleteJob,
) {
    /** 한달이 지난 system action log 삭제 처리 */
    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.delete-system-action-log.initial-delay:100}"
    )
    fun runDeleteJob() {
        CoroutineScope(Dispatchers.IO).launch {
            systemActionLogDeleteJob.runDeleteJob()
        }
    }
}
