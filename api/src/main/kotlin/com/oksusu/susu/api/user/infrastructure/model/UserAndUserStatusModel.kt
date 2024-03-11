package com.oksusu.susu.api.user.infrastructure.model

import com.oksusu.susu.api.user.domain.User
import com.oksusu.susu.api.user.domain.UserStatus
import com.querydsl.core.annotations.QueryProjection

class UserAndUserStatusModel @QueryProjection constructor(
    val user: User,
    val userStatus: UserStatus,
)
