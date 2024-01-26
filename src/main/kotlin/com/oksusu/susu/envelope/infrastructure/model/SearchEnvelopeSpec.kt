package com.oksusu.susu.envelope.infrastructure.model

data class SearchEnvelopeSpec(
    val uid: Long,
    val friendId: Long?,
    val ledgerId: Long?,
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
