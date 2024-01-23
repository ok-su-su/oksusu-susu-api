package com.oksusu.susu.envelope.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.model.response.CreateEnvelopesConfigResponse
import com.oksusu.susu.envelope.model.response.SearchFilterEnvelopeResponse
import com.oksusu.susu.friend.application.RelationshipService
import org.springframework.stereotype.Service

@Service
class EnvelopeConfigService(
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
    private val envelopeService: EnvelopeService,
) {
    suspend fun getCreateEnvelopesConfig(user: AuthUser): CreateEnvelopesConfigResponse {
        val categories = categoryService.getAll()
        val relationships = relationshipService.getAll()

        return CreateEnvelopesConfigResponse(categories, relationships)
    }

    suspend fun getSearchFilter(user: AuthUser): SearchFilterEnvelopeResponse {
        val maxReceivedAmount = envelopeService.findTop1ByUidAndTypeOrderByAmount(
            uid = user.id,
            type = EnvelopeType.RECEIVED
        )?.amount ?: 0L

        return SearchFilterEnvelopeResponse(maxReceivedAmount)
    }
}
