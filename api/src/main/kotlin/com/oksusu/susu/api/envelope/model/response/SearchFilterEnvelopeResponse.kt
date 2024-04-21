package com.oksusu.susu.api.envelope.model.response

data class SearchFilterEnvelopeResponse(
    /** 최소 받은 금액 */
    val minReceivedAmount: Long,
    /** 최대 받은 금액 */
    val maxReceivedAmount: Long,
    /** 최소 보낸 금액 */
    val minSentAmount: Long,
    /** 최대 보낸 금액 */
    val maxSentAmount: Long,
    /** 봉투 총합 */
    val totalAmount: Long,
)
