package com.oksusu.susu.envelope.application

import arrow.fx.coroutines.parZip
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
        return parZip(
            { envelopeService.findTop1ByUidAndTypeOrderByAmountDesc(user.uid, EnvelopeType.RECEIVED) },
            { envelopeService.findTop1ByUidAndTypeOrderByAmountDesc(user.uid, EnvelopeType.SENT) }
        ) { maxReceivedAmount, maxSentAmount ->
            SearchFilterEnvelopeResponse(
                minReceivedAmount = 0L,
                maxReceivedAmount = maxReceivedAmount?.amount ?: 0L,
                minSentAmount = 0L,
                maxSentAmount = maxSentAmount?.amount ?: 0L
            )
        }
    }
}
