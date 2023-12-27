package com.oksusu.susu.community.domain.vo

import jakarta.persistence.*

class VoteSummary(
    val communityId: Long,

    var count: Int = 0,
)
