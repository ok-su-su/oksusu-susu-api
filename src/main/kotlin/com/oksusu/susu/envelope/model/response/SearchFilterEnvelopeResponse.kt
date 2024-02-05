package com.oksusu.susu.envelope.model.response

data class SearchFilterEnvelopeResponse(
    /** 최소 받은 금액 */
    val minReceivedAmount: Long,
    /** 받은 금액 총합 */
    val maxReceivedAmount: Long,
    /** 최소 보낸 금액 */
    val minSentAmount: Long,
    /** 보낸 금액 총합 */
    val maxSentAmount: Long,
)
