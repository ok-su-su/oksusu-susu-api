package com.oksusu.susu.term.model.response

import com.oksusu.susu.term.domain.Term

class GetTermResponse(
    val id: Long,
    val title: String,
    val description: String,
    val isEssential: Boolean,
) {
    companion object {
        fun from(term: Term): GetTermResponse {
            return GetTermResponse(
                id = term.id,
                title = term.title,
                description = term.description,
                isEssential = term.isEssential
            )
        }
    }
}
