package com.oksusu.susu.domain.envelope.infrastructure.model

import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType

data class SearchEnvelopeSpec(
    val uid: Long,
    val friendId: Set<Long>?,
    val friendName: String?,
    val ledgerId: Long?,
    val types: Set<EnvelopeType>?,
    val fromAmount: Long?,
    val toAmount: Long?,
) {
    companion object
}
