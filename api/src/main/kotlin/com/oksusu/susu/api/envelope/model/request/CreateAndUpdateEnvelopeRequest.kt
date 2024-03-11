package com.oksusu.susu.api.envelope.model.request

import com.oksusu.susu.api.category.model.request.CreateCategoryAssignmentRequest
import com.oksusu.susu.api.envelope.domain.vo.EnvelopeType
import java.time.LocalDateTime

data class CreateAndUpdateEnvelopeRequest(
    /** type: SENT, RECEIVED */
    val type: EnvelopeType,
    /** 지인 id */
    val friendId: Long,
    /** 장부 id */
    val ledgerId: Long? = null,
    /** 금액 */
    val amount: Long,
    /** 선물 */
    val gift: String? = null,
    /** 메모 */
    val memo: String? = null,
    /** 방문 : true, 미방문 : false, null인 경우 미선택 */
    val hasVisited: Boolean? = null,
    /** 전달일 */
    val handedOverAt: LocalDateTime,
    /** 카테고리 정보, null인 경우 기타로 등록 */
    val category: CreateCategoryAssignmentRequest?,
)
