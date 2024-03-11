package com.oksusu.susu.api.report.application

import com.oksusu.susu.api.exception.ErrorCode
import com.oksusu.susu.api.exception.NotFoundException
import com.oksusu.susu.api.extension.resolveCancellation
import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.report.domain.ReportMetadata
import com.oksusu.susu.api.report.domain.vo.ReportTargetType
import com.oksusu.susu.api.report.infrastructure.ReportMetadataRepository
import com.oksusu.susu.api.report.model.ReportMetadataModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ReportMetadataService(
    private val reportMetadataRepository: ReportMetadataRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var reportMetadata: Map<Long, ReportMetadataModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-report-metadata.initial-delay:0}"
    )
    fun refreshRelationships() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh report metadata" }

            reportMetadata = runCatching {
                findAllByIsActive(true)
                    .map { metadata -> ReportMetadataModel.from(metadata) }
                    .associateBy { metadata -> metadata.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshReportMetadata", e)
            }.getOrDefault(reportMetadata)

            logger.info { "finish refresh report metadata" }
        }
    }

    suspend fun findAll(targetType: ReportTargetType, isActive: Boolean = true): List<ReportMetadata> {
        return withMDCContext(Dispatchers.IO) {
            reportMetadataRepository.findAllByTargetTypeAndIsActive(targetType, isActive)
        }
    }

    suspend fun findByIdOrThrow(id: Long): ReportMetadata {
        return findByIdOrNull(id) ?: throw NotFoundException(ErrorCode.NOT_FOUND_REPORT_METADATA_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): ReportMetadata? {
        return withMDCContext(Dispatchers.IO) { reportMetadataRepository.findByIdOrNull(id) }
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<ReportMetadata> {
        return withMDCContext(Dispatchers.IO) { reportMetadataRepository.findAllByIsActive(isActive) }
    }

    suspend fun getAllTargetType(targetType: ReportTargetType): List<ReportMetadataModel> {
        return this.reportMetadata.values
            .filter { metadata -> metadata.targetType == targetType }
            .sortedBy { metadata -> metadata.seq }
    }

    suspend fun get(id: Long): ReportMetadataModel {
        return this.reportMetadata[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_REPORT_METADATA_ERROR)
    }
}
