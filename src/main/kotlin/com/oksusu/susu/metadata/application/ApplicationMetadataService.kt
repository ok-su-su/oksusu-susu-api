package com.oksusu.susu.metadata.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.metadata.domain.ApplicationMetadata
import com.oksusu.susu.metadata.infrastructure.ApplicationMetadataRepository
import com.oksusu.susu.metadata.model.ApplicationMetadataModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ApplicationMetadataService(
    private val applicationMetadataRepository: ApplicationMetadataRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var applicationMetadata: ApplicationMetadataModel? = null

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-application-metadata.initial-delay:0}"
    )
    fun refreshApplicationMetadata() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh application metadata" }

            applicationMetadata = runCatching {
                val metadata = findTop1ByIsActiveOrderByCreatedAtDesc(true)

                ApplicationMetadataModel(
                    id = metadata.id,
                    applicationVersion = metadata.applicationVersion,
                    forcedUpdateDate = metadata.forcedUpdateDate
                )
            }.onFailure { e ->
                logger.resolveCancellation("refreshCategories", e)
            }.getOrDefault(applicationMetadata)

            logger.info { "finish refresh application metadata" }
        }
    }

    suspend fun findTop1ByIsActiveOrderByCreatedAtDesc(isActive: Boolean): ApplicationMetadata {
        return withMDCContext(Dispatchers.IO) {
            applicationMetadataRepository.findTop1ByIsActiveOrderByCreatedAtDesc(isActive)
        }
    }

    fun getApplicationMetadata(): ApplicationMetadataModel {
        return applicationMetadata ?: throw NotFoundException(ErrorCode.NOT_FOUND_APPLICATION_METADATA_ERROR)
    }
}
