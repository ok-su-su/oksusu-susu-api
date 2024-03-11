package com.oksusu.susu.api.envelope.infrastructure.model

import com.oksusu.susu.api.envelope.domain.Envelope
import com.oksusu.susu.api.friend.domain.Relationship
import com.querydsl.core.annotations.QueryProjection

class EnvelopeAndRelationshipModel @QueryProjection constructor(
    val envelope: Envelope,
    val relationship: Relationship,
)
