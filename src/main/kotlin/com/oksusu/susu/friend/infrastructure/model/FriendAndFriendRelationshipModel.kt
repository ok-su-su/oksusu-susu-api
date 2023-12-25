package com.oksusu.susu.friend.infrastructure.model

import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.FriendRelationship
import com.querydsl.core.annotations.QueryProjection

data class FriendAndFriendRelationshipModel @QueryProjection constructor(
    val friend: Friend,
    val friendRelationship: FriendRelationship,
)
