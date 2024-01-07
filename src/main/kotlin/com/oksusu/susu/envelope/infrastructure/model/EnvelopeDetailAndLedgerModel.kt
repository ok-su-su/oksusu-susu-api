package com.oksusu.susu.envelope.infrastructure.model

import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.ledger.domain.Ledger
import com.querydsl.core.annotations.QueryProjection

data class EnvelopeDetailAndLedgerModel @QueryProjection constructor(
    val ledger: Ledger?,
    val envelope: Envelope,
    val friend: Friend,
    val friendRelationship: FriendRelationship,
    val categoryAssignment: CategoryAssignment,
)
