package com.oksusu.susu.envelope.infrastructure.model

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.friend.domain.Friend
import com.querydsl.core.annotations.QueryProjection

class EnvelopeAndFriendModel @QueryProjection constructor(
    val envelope: Envelope,
    val friend: Friend,
)
