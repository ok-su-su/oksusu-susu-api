package com.oksusu.susu.envelope.model.request

import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.IncludeSpec

data class SearchEnvelopeRequest(
    val friendId: Long?,
    val ledgerId: Long?,
    val types: Set<EnvelopeType>?,
    val include: Set<IncludeSpec>?,
    val fromAmount: Long?,
    val toAmount: Long?,
)
