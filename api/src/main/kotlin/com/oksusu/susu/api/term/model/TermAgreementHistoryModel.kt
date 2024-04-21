package com.oksusu.susu.api.term.model

import com.oksusu.susu.domain.common.BaseEntity
import com.oksusu.susu.domain.term.domain.vo.TermAgreementChangeType

/** 약관 동의 정보 기록 모델*/
data class TermAgreementHistoryModel(
    /** 약관 동의 정보 기록 id */
    val id: Long,
    /** 약관 동의한 유저 id */
    val uid: Long,
    /** 약관 정보 id */
    val termId: Long,
    /** 약관 동의 여부 변화 종류 */
    val changeType: TermAgreementChangeType,
) : BaseEntity()
