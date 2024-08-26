package com.oksusu.susu.api.metadata.application

import com.oksusu.susu.api.metadata.model.ApplicationMetadataModel
import com.oksusu.susu.api.metadata.model.DeviceOS
import com.oksusu.susu.api.metadata.model.response.CheckApplicationVersionResposne
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.resolveCancellation
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata
import com.oksusu.susu.domain.metadata.infrastructure.ApplicationMetadataRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ApplicationMetadataService(
    private val applicationMetadataRepository: ApplicationMetadataRepository,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) {
    private val logger = KotlinLogging.logger { }
    private var metadata: ApplicationMetadataModel = ApplicationMetadataModel(
        id = 1L,
        iosMinSupportVersion = "0.0.0",
        aosMinSupportVersion = "0.0.0",
        isActive = true
    )

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-application-metadata.initial-delay:0}"
    )
    fun refreshApplicationMetadata() {
        CoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler).launch {
            logger.info { "start refresh applicationMetadata" }

            metadata = runCatching {
                findTop1ByIsActiveOrderByCreatedAtDescOrThrow()
                    .run { ApplicationMetadataModel.from(this) }
            }.onFailure { e ->
                logger.resolveCancellation("refreshCategories", e)
            }.getOrDefault(metadata)

            logger.info { "finish refresh applicationMetadata" }
        }
    }

    fun getMetadata(): ApplicationMetadataModel {
        return metadata
    }

    fun checkApplicationVersion(deviceOS: DeviceOS, version: String): CheckApplicationVersionResposne {
        val needForceUpdate = when (deviceOS) {
            DeviceOS.AOS -> compareVersion(version, metadata.aosMinSupportVersion)
            DeviceOS.IOS -> compareVersion(version, metadata.iosMinSupportVersion)
        }.run { this < 0 }

        return CheckApplicationVersionResposne(
            needForceUpdate = needForceUpdate
        )
    }

    /**
     * version1 < version2 : -1
     *
     * version1 = version2 : 0
     *
     * version1 > version2 : 1
     */
    private fun compareVersion(version1: String, version2: String): Int {
        val parsedVersion1 = version1.split(".")
        val parsedVersion2 = version2.split(".")

        for (i in 0..2) {
            if (parsedVersion1[i] > parsedVersion2[i]) {
                return 1
            } else if (parsedVersion1[i] < parsedVersion2[i]) {
                return -1
            }
        }
        return 0
    }

    suspend fun findTop1ByIsActiveOrderByCreatedAtDescOrThrow(): ApplicationMetadata {
        return findTop1ByIsActiveOrderByCreatedAtDescOrNull(true)
            ?: throw NotFoundException(ErrorCode.NOT_FOUND_APPLICATION_METADATA_ERROR)
    }

    suspend fun findTop1ByIsActiveOrderByCreatedAtDescOrNull(isActive: Boolean): ApplicationMetadata? {
        return withMDCContext(Dispatchers.IO) {
            applicationMetadataRepository.findTop1ByIsActiveOrderByCreatedAtDesc(isActive)
        }
    }
}
