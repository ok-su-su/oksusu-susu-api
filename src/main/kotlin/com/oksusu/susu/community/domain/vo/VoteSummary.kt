package com.oksusu.susu.community.domain.vo

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

class VoteSummary(
    val communityId: Long,

    var count: Int = 0,
)
