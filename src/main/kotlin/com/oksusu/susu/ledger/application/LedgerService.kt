package com.oksusu.susu.ledger.application

import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.infrastructure.LedgerRepository
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerModel
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LedgerService(
    private val ledgerRepository: LedgerRepository,
) {
    @Transactional
    fun saveSync(ledger: Ledger): Ledger {
        return ledgerRepository.save(ledger)
    }

    @Transactional
    fun deleteSync(ledger: Ledger) {
        ledgerRepository.delete(ledger)
    }

    suspend fun search(
        searchSpec: SearchLedgerSpec,
        pageable: Pageable,
    ): Page<SearchLedgerModel> {
        return withContext(Dispatchers.IO) {
            ledgerRepository.search(searchSpec, pageable)
        }
    }

    suspend fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Ledger> {
        return withContext(Dispatchers.IO) { ledgerRepository.findAllByUidAndIdIn(uid, ids) }
    }
}
