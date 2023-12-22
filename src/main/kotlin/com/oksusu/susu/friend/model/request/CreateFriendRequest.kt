package com.oksusu.susu.friend.model.request

data class CreateFriendRequest(
    val name: String,
    val phoneNumber: String?,
    val relationshipId: Long,
    val customRelation: String?,
)
