package com.oksusu.susu.event.model

import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.term.domain.TermAgreement
import com.oksusu.susu.term.domain.vo.TermAgreementChangeType

sealed interface Event

data class DeleteLedgerEvent(
    val ledger: Ledger,
) : Event

class TermAgreementHistoryCreateEvent(
    val termAgreements: List<TermAgreement>,
    val changeType: TermAgreementChangeType,
): BaseEvent(), Event