package com.oksusu.susu.post.model.vo

data class SearchVoteRequest(
    val content: String?,
    val mine: Boolean?,
    val sortType: VoteSortType?,
    val categoryId: Long?,
)

enum class VoteSortType {
    /** 최신 작성 순 */
    LATEST,

    /** 투표 많은 순 */
    POPULAR,
    ;
}
