package com.oksusu.susu.common.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class SusuPageRequest(
    val page: Int?,
    val size: Int?,
    /** createdAt, desc */
    val sort: String?,
) {
    fun toDefault(): Pageable {
        val page = this.page ?: 0
        val size = this.size ?: 10
        val sort = this.sort.parsingSort()

        return PageRequest.of(page, size, sort)
    }

    fun String?.parsingSort(): Sort {
        val sortSpec = (this ?: "createdAt, desc").trim().split(",")

        if (sortSpec.size != 2) {
            return Sort.by("createdAt").descending()
        }

        val requestedSortField = Sort.Order.by(sortSpec[0])
        val direction = Sort.Direction.fromString(sortSpec[1])

        return Sort.by(requestedSortField.with(direction))
    }
}
