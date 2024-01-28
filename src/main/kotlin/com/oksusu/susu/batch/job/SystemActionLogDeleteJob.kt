package com.oksusu.susu.batch.job

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.log.application.SystemActionLogService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SystemActionLogDeleteJob(
    private val systemActionLogService: SystemActionLogService,
    private val txTemplates: TransactionTemplates,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DELETE_BEFORE_DAYS = 30L
        private const val DELETE_CHUNK = 1000
    }

    suspend fun runDeleteJob() {
        val targetDate = LocalDateTime.now().minusDays(DELETE_BEFORE_DAYS)

        val targetLog = txTemplates.writer.coExecute {
            systemActionLogService.findAllByCreatedAtBefore(targetDate)
        }.takeIf { logs -> logs.isNotEmpty() } ?: return

        targetLog
            .chunked(DELETE_CHUNK)
            .forEach { logs ->
                systemActionLogService.deleteAllBy(logs)
            }

        logger.info { "delete system action log counts: ${targetLog.size}" }
    }
}
