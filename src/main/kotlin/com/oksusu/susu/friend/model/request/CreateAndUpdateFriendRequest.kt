package com.oksusu.susu.friend.model.request

data class CreateAndUpdateFriendRequest(
    /** 지인 이름 */
    val name: String,
    /** 전화번호 */
    val phoneNumber: String?,
    /** 관계 정보 id */
    val relationshipId: Long,
    /** 커스텀 관계 정보 */
    val customRelation: String?,
)
