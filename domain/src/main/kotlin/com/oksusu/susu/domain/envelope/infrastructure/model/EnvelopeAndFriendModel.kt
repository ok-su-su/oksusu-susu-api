package com.oksusu.susu.domain.envelope.infrastructure.model

import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.friend.domain.Friend
import com.querydsl.core.annotations.QueryProjection

class EnvelopeAndFriendModel @QueryProjection constructor(
    val envelope: Envelope,
    val friend: Friend,
)
