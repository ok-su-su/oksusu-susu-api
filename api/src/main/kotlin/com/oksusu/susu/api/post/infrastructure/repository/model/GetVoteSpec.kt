package com.oksusu.susu.api.post.infrastructure.repository.model

import org.springframework.data.domain.Pageable

/** 투표 조회 조건 */
class GetVoteSpec(
    /** 유저 id */
    val uid: Long,
    /** 투표 검색 조건 */
    val searchSpec: SearchVoteSpec,
    /** 차단된 유저 id */
    val userBlockIds: Set<Long>,
    /** 차단된 게시글 id */
    val postBlockIds: Set<Long>,
    /** pageable */
    val pageable: Pageable,
)
