package com.oksusu.susu.post.infrastructure.repository.model

import org.springframework.data.domain.Pageable

class GetAllVoteSpec(
    val uid: Long,
    val searchSpec: SearchVoteSpec,
    val userBlockIds: Set<Long>,
    val postBlockIds: Set<Long>,
    val pageable: Pageable,
)
