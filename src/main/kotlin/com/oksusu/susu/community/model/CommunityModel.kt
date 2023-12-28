package com.oksusu.susu.community.model

import com.oksusu.susu.community.domain.vo.CommunityType

class CommunityModel(
    val id: Long,
    val uid: Long,
    val type: CommunityType,
    val title: String?,
    val content: String,
    val isActive: Boolean,
)