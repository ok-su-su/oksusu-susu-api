package com.oksusu.susu.domain.envelope.infrastructure.model

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class FriendStatisticsModel @QueryProjection constructor(
    val friendId: Long,
    val sentAmounts: Long,
    val receivedAmounts: Long,
    val handedOverAt: LocalDateTime,
)
