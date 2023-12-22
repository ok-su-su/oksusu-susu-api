package com.oksusu.susu.friend.model

import com.oksusu.susu.friend.domain.Friend

data class FriendModel(
    val id: Long,
    val name: String,
    val phoneNumber: String?,
) {
    companion object {
        fun from(friend: Friend): FriendModel {
            return FriendModel(
                id = friend.id,
                name = friend.name,
                phoneNumber = friend.phoneNumber
            )
        }
    }
}
