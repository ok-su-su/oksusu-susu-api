package com.oksusu.susu.batch.log.job

import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.log.infrastructure.SystemActionLogRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SystemActionLogDeleteJob(
    private val txTemplates: TransactionTemplates,
    private val systemActionLogRepository: SystemActionLogRepository,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DELETE_BEFORE_DAYS = 30L
        private const val DELETE_CHUNK = 1000
    }

    suspend fun runDeleteJob() {
        val targetDate = LocalDateTime.now().minusDays(DELETE_BEFORE_DAYS)

        val targetLog = txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
            systemActionLogRepository.findAllByCreatedAtBefore(targetDate)
        }.takeIf { logs -> logs.isNotEmpty() } ?: return

        targetLog
            .chunked(DELETE_CHUNK)
            .forEach { logs ->
                systemActionLogRepository.deleteAllInBatch(logs)
            }

        logger.info { "delete system action log counts: ${targetLog.size}" }
    }
}
