package com.oksusu.susu.post.domain.vo

import jakarta.persistence.*

class VoteSummary(
    val postId: Long,

    var count: Int = 0,
)
