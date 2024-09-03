package com.oksusu.susu.api.envelope.model.request

import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType

data class SearchEnvelopeRequest(
    /** 지인 id */
    val friendIds: Set<Long>?,
    /** 지인 이름 */
    val friendName: String?,
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
) {
    enum class IncludeSpec {
        CATEGORY,
        FRIEND,
        RELATIONSHIP,
        FRIEND_RELATIONSHIP,
        ;
    }
}
