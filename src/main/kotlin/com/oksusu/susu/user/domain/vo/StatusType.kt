package com.oksusu.susu.user.domain.vo

/** 상태 타입 */
enum class StatusType {
    /** 활동 */
    ACTIVE,

    /** 탈퇴 */
    DELETED,

    /** 일시 정지 7일 */
    RESTRICTED_7_DAYS,

    /** 영구 정지 */
    BANISHED,
    ;
}
