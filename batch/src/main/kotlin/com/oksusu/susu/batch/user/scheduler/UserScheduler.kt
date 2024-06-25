package com.oksusu.susu.batch.user.scheduler

import com.oksusu.susu.batch.user.job.DeleteWithdrawUserDataJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserScheduler(
    val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun deleteWithdrawUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            deleteWithdrawUserDataJob.deleteWithdrawUserData()
        }
    }
}
