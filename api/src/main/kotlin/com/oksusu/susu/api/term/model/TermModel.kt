package com.oksusu.susu.api.term.model

/** 약관 정보 모델 */
data class TermModel(
    /** 약관 정보 id */
    val id: Long,
    /** 약관 제목 */
    val title: String,
    /** 약관 내용 */
    val description: String,
    /** 필수 동의 여부 / 필수 : 1, 선택 : 0 */
    val isEssential: Boolean,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    val isActive: Boolean,
)
