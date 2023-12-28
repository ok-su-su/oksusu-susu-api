package com.oksusu.susu.term.model.event

import com.oksusu.susu.common.dto.BaseEvent
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType

class TermAgreementHistoryCreateEvent(
    val termAgreements: List<TermAgreement>,
    val changeType: TermAgreementChangeType,
): BaseEvent()