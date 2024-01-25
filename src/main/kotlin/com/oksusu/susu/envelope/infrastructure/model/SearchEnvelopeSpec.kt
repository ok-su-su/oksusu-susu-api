package com.oksusu.susu.envelope.infrastructure.model

data class SearchEnvelopeSpec(
    val uid: Long,
    val friendId: Long?,
    val ledgerId: Long?,
    val include: Set<IncludeSpec>,
)

enum class IncludeSpec {
    CATEGORY,
    FRIEND,
    RELATION,
    ;
}
