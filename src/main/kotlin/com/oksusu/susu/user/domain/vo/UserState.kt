package com.oksusu.susu.user.domain.vo

/** 유저 계정 상태 */
enum class UserState {
    /** 활동 유저 */
    ACTIVE,

    /** 탈퇴한 유저 */
    DELETED,

    /** 계정 정지 */
    RESTRICTED,

    /** 영구정지 */
    BANISHED,
    ;
}
