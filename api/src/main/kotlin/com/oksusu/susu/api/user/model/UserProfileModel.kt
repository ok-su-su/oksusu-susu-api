package com.oksusu.susu.api.user.model

import com.oksusu.susu.api.user.domain.User

/** 유저 profile */
data class UserProfileModel(
    /** 유저 id */
    val id: Long,
    /** 이름 */
    val name: String,
    /** 프로필 사진 url */
    val profileImageUrl: String?,
) {
    companion object {
        fun from(user: User): UserProfileModel {
            return UserProfileModel(
                id = user.id,
                name = user.name,
                profileImageUrl = user.profileImageUrl
            )
        }
    }
}
