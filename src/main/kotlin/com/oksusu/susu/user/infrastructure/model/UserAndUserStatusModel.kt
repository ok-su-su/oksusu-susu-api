package com.oksusu.susu.user.infrastructure.model

import com.oksusu.susu.user.domain.User
import com.oksusu.susu.user.domain.UserStatus
import com.querydsl.core.annotations.QueryProjection

class UserAndUserStatusModel @QueryProjection constructor(
    val user: User,
    val userStatus: UserStatus,
)