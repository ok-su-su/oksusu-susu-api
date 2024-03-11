package com.oksusu.susu.api.envelope.infrastructure.model

import com.oksusu.susu.api.category.domain.CategoryAssignment
import com.oksusu.susu.api.envelope.domain.Envelope
import com.oksusu.susu.api.friend.domain.Friend
import com.oksusu.susu.api.friend.domain.FriendRelationship
import com.querydsl.core.annotations.QueryProjection

data class EnvelopeDetailModel @QueryProjection constructor(
    val envelope: Envelope,
    val friend: Friend,
    val friendRelationship: FriendRelationship,
    val categoryAssignment: CategoryAssignment,
)
