package com.oksusu.susu.api.metadata.application

import com.oksusu.susu.api.metadata.model.response.ApplicationVersionMetadataResponse
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata
import com.oksusu.susu.domain.metadata.infrastructure.ApplicationMetadataRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class ApplicationMetadataService(
    private val applicationMetadataRepository: ApplicationMetadataRepository,
) {
    suspend fun getApplicationVersionMetadata(): ApplicationVersionMetadataResponse {
        val applicationMetadata = findTop1ByIsActiveOrderByCreatedAtDescOrThrow()
        return ApplicationVersionMetadataResponse.from(applicationMetadata)
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
