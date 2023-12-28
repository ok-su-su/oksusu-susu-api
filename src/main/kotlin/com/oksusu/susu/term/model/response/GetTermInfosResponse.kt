package com.oksusu.susu.term.model.response

import com.oksusu.susu.term.domain.Term

class GetTermInfosResponse(
    val id: Long,
    val title: String,
    val isEssential: Boolean,
) {
    companion object {
        fun from(term: Term): GetTermInfosResponse {
            return GetTermInfosResponse(
                id = term.id,
                title = term.title,
                isEssential = term.isEssential
            )
        }
    }
}
