package com.oksusu.susu.metadata.application

import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.metadata.model.response.ApplicationVersionMetadataResponse
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
