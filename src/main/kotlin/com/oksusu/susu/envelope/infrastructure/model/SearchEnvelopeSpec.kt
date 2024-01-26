package com.oksusu.susu.envelope.infrastructure.model

import com.oksusu.susu.envelope.domain.vo.EnvelopeType

data class SearchEnvelopeSpec(
    val uid: Long,
    val friendId: Long?,
    val ledgerId: Long?,
    val types: Set<EnvelopeType>?,
    val include: Set<IncludeSpec>,
    val fromAmount: Long?,
    val toAmount: Long?,
)

enum class IncludeSpec {
    CATEGORY,
    FRIEND,
    RELATION,
    ;
}
