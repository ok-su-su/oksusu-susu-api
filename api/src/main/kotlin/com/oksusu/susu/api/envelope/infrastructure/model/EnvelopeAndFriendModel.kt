package com.oksusu.susu.api.envelope.infrastructure.model

import com.oksusu.susu.api.envelope.domain.Envelope
import com.oksusu.susu.api.friend.domain.Friend
import com.querydsl.core.annotations.QueryProjection

class EnvelopeAndFriendModel @QueryProjection constructor(
    val envelope: Envelope,
    val friend: Friend,
)
