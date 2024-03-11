package com.oksusu.susu.api.metadata.application

import com.oksusu.susu.api.config.database.TransactionTemplates
import com.oksusu.susu.api.metadata.model.response.ApplicationVersionMetadataResponse
import org.springframework.stereotype.Service

@Service
class ApplicationMetadataFacade(
    private val applicationMetadataService: ApplicationMetadataService,
    private val txTemplates: TransactionTemplates,
) {
    fun getApplicationVersionMetadata(): ApplicationVersionMetadataResponse {
        val applicationMetadata = applicationMetadataService.getApplicationMetadata()

        return ApplicationVersionMetadataResponse(
            applicationVersion = applicationMetadata.applicationVersion,
            forcedUpdateDate = applicationMetadata.forcedUpdateDate
        )
    }
}
