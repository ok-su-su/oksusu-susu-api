package com.oksusu.susu.envelope.model.request

import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.IncludeSpec

data class SearchEnvelopeRequest(
    /** 지인 id */
    val friendId: Long?,
    /** 장부 id */
    val ledgerId: Long?,
    /** type: SENT, RECEIVED */
    val types: Set<EnvelopeType>?,
    /** 포함할 데이터 목록 */
    val include: Set<IncludeSpec>?,
    /** 금액 조건 from */
    val fromAmount: Long?,
    /** 금액 조건 to */
    val toAmount: Long?,
)
