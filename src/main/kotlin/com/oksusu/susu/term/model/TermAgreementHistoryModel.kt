package com.oksusu.susu.term.model

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType

data class TermAgreementHistoryModel(
    val id: Long,
    val uid: Long,
    val termId: Long,
    val changeType: TermAgreementChangeType,
) : BaseEntity()
