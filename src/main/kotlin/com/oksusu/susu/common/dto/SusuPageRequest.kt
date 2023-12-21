package com.oksusu.susu.common.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class SusuPageRequest(
    val page: Int?,
    val size: Int?,
) {
    fun toDefault(): Pageable {
        val page = this.page ?: 0
        val size = this.size ?: 0
        val sort = Sort.by("createdAt").descending()

        return PageRequest.of(page, size, sort)
    }
}
