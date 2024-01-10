package com.oksusu.susu.post.infrastructure.repository.model

import org.springframework.data.domain.Pageable

class GetAllVoteSpec(
    val uid: Long,
    val searchSpec: SearchVoteSpec,
    val userBlockIds: List<Long>,
    val postBlockIds: List<Long>,
    val pageable: Pageable,
)
