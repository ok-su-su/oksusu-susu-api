package com.oksusu.susu.api.friend.model.request

data class SearchFriendRequest(
    /** 지인 이름 */
    val name: String? = null,
    /** 전화번호 */
    val phoneNumber: String? = null,
)
