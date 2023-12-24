package com.oksusu.susu.user.model

import com.oksusu.susu.user.domain.User

class UserProfileModel(
    val id: Long,
    val name: String,
    val profileImageUrl: String?,
) {
    companion object {
        fun from(user: User): UserProfileModel {
            return UserProfileModel(
                id = user.id,
                name = user.name,
                profileImageUrl = user.profileImageUrl,
            )
        }
    }
}