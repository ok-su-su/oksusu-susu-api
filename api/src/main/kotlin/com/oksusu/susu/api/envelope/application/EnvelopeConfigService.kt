package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.envelope.model.response.CreateEnvelopesConfigResponse
import com.oksusu.susu.api.envelope.model.response.SearchFilterEnvelopeResponse
import com.oksusu.susu.api.friend.application.RelationshipService
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import org.springframework.stereotype.Service

@Service
class EnvelopeConfigService(
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
    private val envelopeService: com.oksusu.susu.api.envelope.application.EnvelopeService,
) {
    suspend fun getCreateEnvelopesConfig(user: AuthUser): CreateEnvelopesConfigResponse {
        val categories = categoryService.getAll()
        val relationships = relationshipService.getAll()

        return CreateEnvelopesConfigResponse(categories, relationships)
    }

    suspend fun getSearchFilter(user: AuthUser): SearchFilterEnvelopeResponse {
        return parZipWithMDC(
            { envelopeService.findTop1ByUidAndTypeOrderByAmountAsc(user.uid, EnvelopeType.RECEIVED) },
            { envelopeService.findTop1ByUidAndTypeOrderByAmountDesc(user.uid, EnvelopeType.RECEIVED) },
            { envelopeService.findTop1ByUidAndTypeOrderByAmountAsc(user.uid, EnvelopeType.SENT) },
            { envelopeService.findTop1ByUidAndTypeOrderByAmountDesc(user.uid, EnvelopeType.SENT) },
            { envelopeService.countTotalAmountByUid(user.uid) }
        ) { minReceivedAmount, maxReceivedAmount, minSentAmount, maxSentAmount, totalAmount ->
            SearchFilterEnvelopeResponse(
                minReceivedAmount = minReceivedAmount?.amount ?: 0L,
                maxReceivedAmount = maxReceivedAmount?.amount ?: 0L,
                minSentAmount = minSentAmount?.amount ?: 0L,
                maxSentAmount = maxSentAmount?.amount ?: 0L,
                totalAmount = totalAmount ?: 0L
            )
        }
    }
}
