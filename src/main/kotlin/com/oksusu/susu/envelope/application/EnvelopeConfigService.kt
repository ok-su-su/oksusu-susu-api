package com.oksusu.susu.envelope.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.model.response.CreateEnvelopesConfigResponse
import com.oksusu.susu.envelope.model.response.SearchFilterEnvelopeResponse
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.friend.application.RelationshipService
import kotlinx.coroutines.Dispatchers
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
            Dispatchers.IO.withMDCContext(),
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
                totalAmount = totalAmount
            )
        }
    }
}
