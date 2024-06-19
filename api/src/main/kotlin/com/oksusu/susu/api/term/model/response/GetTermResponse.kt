package com.oksusu.susu.api.term.model.response

import com.oksusu.susu.domain.term.domain.Term

data class GetTermResponse(
    /** 약관 정보 id */
    val id: Long,
    /** 약관 제목 */
    val title: String,
    /** 약관 내용 */
    val description: String,
    /** 필수 동의 여부 / 필수 : 1, 선택 : 0 */
    val isEssential: Boolean,
) {
    companion object {
        fun from(term: Term): GetTermResponse {
            return GetTermResponse(
                id = term.id,
                title = term.title,
                description = term.description ?: "",
                isEssential = term.isEssential
            )
        }
    }
}
