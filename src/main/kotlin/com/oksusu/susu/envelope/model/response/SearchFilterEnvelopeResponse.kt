package com.oksusu.susu.envelope.model.response

data class SearchFilterEnvelopeResponse(
    /** 최소 금액 */
    val minReceivedAmount: Long,
    /** 받은 금액 총합 */
    val maxReceivedAmount: Long,
)
