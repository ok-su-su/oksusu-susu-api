package com.oksusu.susu.envelope.model.request

import com.oksusu.susu.envelope.infrastructure.model.IncludeSpec

data class SearchEnvelopeRequest(
    val ledgerId: Long?,
    val include: Set<IncludeSpec>?,
)
