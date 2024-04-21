package com.oksusu.susu.domain.envelope.infrastructure.model

import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.querydsl.core.annotations.QueryProjection

data class EnvelopeDetailModel @QueryProjection constructor(
    val envelope: Envelope,
    val friend: Friend,
    val friendRelationship: FriendRelationship,
    val categoryAssignment: CategoryAssignment,
)
