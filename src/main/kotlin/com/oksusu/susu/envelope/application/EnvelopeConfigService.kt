package com.oksusu.susu.envelope.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.model.response.CreateEnvelopesConfigResponse
import com.oksusu.susu.friend.application.RelationshipService
import org.springframework.stereotype.Service

@Service
class EnvelopeConfigService(
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
) {
    suspend fun getCreateEnvelopesConfig(user: AuthUser): CreateEnvelopesConfigResponse {
        val categories = categoryService.getAll()
        val relationships = relationshipService.getAll()

        return CreateEnvelopesConfigResponse(categories, relationships)
    }
}
