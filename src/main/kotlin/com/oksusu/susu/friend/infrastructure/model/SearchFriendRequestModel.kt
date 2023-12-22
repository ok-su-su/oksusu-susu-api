package com.oksusu.susu.friend.infrastructure.model

data class SearchFriendRequestModel(
    val uid: Long,
    val name: String?,
    val phoneNumber: String?,
)
