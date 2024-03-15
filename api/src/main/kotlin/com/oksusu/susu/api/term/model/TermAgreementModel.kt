package com.oksusu.susu.api.term.model

/** 약관 정보 동의 모델 */
data class TermAgreementModel(
    /** 약관 정보 동의 id */
    val id: Long,
    /** 약관 동의한 유저 id */
    val uid: Long,
    /** 약관 정보 id */
    val termId: Long,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    var isActive: Boolean = true,
)
