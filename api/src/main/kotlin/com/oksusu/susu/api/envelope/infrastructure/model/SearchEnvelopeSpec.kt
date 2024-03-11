package com.oksusu.susu.api.envelope.infrastructure.model

import com.oksusu.susu.api.envelope.domain.vo.EnvelopeType

data class SearchEnvelopeSpec(
    val uid: Long,
    val friendId: Set<Long>?,
    val ledgerId: Long?,
    val types: Set<EnvelopeType>?,
    val fromAmount: Long?,
    val toAmount: Long?,
)
