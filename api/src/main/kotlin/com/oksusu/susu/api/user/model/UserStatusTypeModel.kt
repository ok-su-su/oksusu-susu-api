package com.oksusu.susu.api.user.model

import com.oksusu.susu.domain.user.domain.UserStatusType
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo

/** 유저 상태 정보 타입 */
data class UserStatusTypeModel(
    val id: Long = -1,
    /** 상태 정보 타입 정보 */
    val statusTypeInfo: UserStatusTypeInfo,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    val isActive: Boolean,
) {
    companion object {
        fun from(userStatusType: UserStatusType): UserStatusTypeModel {
            return UserStatusTypeModel(
                id = userStatusType.id,
                statusTypeInfo = userStatusType.statusTypeInfo,
                isActive = userStatusType.isActive
            )
        }
    }
}
