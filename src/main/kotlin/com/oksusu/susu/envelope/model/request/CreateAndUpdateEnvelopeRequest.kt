package com.oksusu.susu.envelope.model.request

import com.oksusu.susu.category.model.request.CreateCategoryAssignmentRequest
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import java.time.LocalDateTime

data class CreateAndUpdateEnvelopeRequest(
    val type: EnvelopeType,
    val friendId: Long,
    val amount: Long,
    val gift: String? = null,
    val memo: String? = null,
    val hasVisited: Boolean,
    val handedOverAt: LocalDateTime,
    val category: CreateCategoryAssignmentRequest,
)
