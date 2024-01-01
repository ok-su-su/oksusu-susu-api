package com.oksusu.susu.envelope.infrastructure.model

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.friend.domain.Relationship
import com.querydsl.core.annotations.QueryProjection

class EnvelopeAndRelationshipModel @QueryProjection constructor(
    val envelope: Envelope,
    val relationship: Relationship,
)
