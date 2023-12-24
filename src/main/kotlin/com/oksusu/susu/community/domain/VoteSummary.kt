package com.oksusu.susu.community.domain

import com.oksusu.susu.common.domain.BaseEntity
import jakarta.persistence.*

class VoteSummary(
    val communityId: Long,

    val count: Int = 0,
)