package com.oksusu.susu.domain.common.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class SusuPageRequest(
    /** page, 0부터 시작 */
    val page: Int?,
    /** size, default is 10 */
    val size: Int?,
    /**
     * **정렬조건**
     * - ex: createdAt, desc
     * - 각 api의 정렬 조건에 맞추어 진행
     */
    val sort: String?,
) {
    fun toDefault(): Pageable {
        val page = this.page ?: 0
        val size = this.size ?: 10
        val sort = this.sort.parsingSort()

        return PageRequest.of(page, size, sort)
    }

    fun String?.parsingSort(): Sort {
        val sortSpec = (this ?: "createdAt,desc").trim().split(",")

        if (sortSpec.size != 2) {
            return Sort.by("createdAt").descending()
        }

        val requestedSortField = Sort.Order.by(sortSpec[0].trim())
        val direction = Sort.Direction.fromString(sortSpec[1].trim())

        return Sort.by(requestedSortField.with(direction))
    }
}
