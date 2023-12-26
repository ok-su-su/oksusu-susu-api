package com.oksusu.susu.community.model.vo

import com.oksusu.susu.community.domain.vo.CommunityCategory

data class VoteSortRequest(
    val mine: Boolean,
    val sortType: VoteSortType,
    val category: CommunityCategory,
)

enum class VoteSortType {
    // 최신 작성 순
    LATEST,

    // 투표 많은 순
    POPULAR,
    ;
}
