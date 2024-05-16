package com.oksusu.susu.domain.envelope.infrastructure.model

import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.friend.domain.Relationship
import com.querydsl.core.annotations.QueryProjection

data class EnvelopeAndRelationshipModel @QueryProjection constructor(
    val envelope: Envelope,
    val relationship: Relationship,
)
