package com.oksusu.susu.api.friend.model

import com.oksusu.susu.domain.friend.domain.Friend

data class FriendModel(
    /** 지인 id */
    val id: Long,
    /** 지인 이름 */
    val name: String,
    /** 지인 전화번호 */
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
