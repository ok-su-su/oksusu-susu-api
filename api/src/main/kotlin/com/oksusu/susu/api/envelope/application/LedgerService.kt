package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.domain.envelope.infrastructure.model.LedgerDetailModel
import com.oksusu.susu.domain.envelope.infrastructure.model.SearchLedgerModel
import com.oksusu.susu.domain.envelope.infrastructure.model.SearchLedgerSpec
import kotlinx.coroutines.Dispatchers
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
        return withMDCContext(Dispatchers.IO) {
            ledgerRepository.search(searchSpec, pageable)
        }
    }

    suspend fun findByIdAndUidOrThrow(id: Long, uid: Long): Ledger {
        return findByIdAndUidOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_LEDGER_ERROR)
    }

    suspend fun findByIdAndUidOrNull(id: Long, uid: Long): Ledger? {
        return withMDCContext(Dispatchers.IO) { ledgerRepository.findByIdAndUid(id, uid) }
    }

    suspend fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Ledger> {
        return withMDCContext(Dispatchers.IO) { ledgerRepository.findAllByUidAndIdIn(uid, ids) }
    }

    suspend fun findLedgerDetailOrThrow(id: Long, uid: Long): LedgerDetailModel {
        return findLedgerDetailOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_LEDGER_ERROR)
    }

    suspend fun findLedgerDetailOrNull(id: Long, uid: Long): LedgerDetailModel? {
        return withMDCContext(Dispatchers.IO) { ledgerRepository.findLedgerDetail(id, uid) }
    }

    suspend fun countPerCategoryId(): List<CountPerCategoryIdModel> {
        return withMDCContext(Dispatchers.IO) {
            ledgerRepository.countPerCategoryId()
        }
    }

    suspend fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel> {
        return withMDCContext(Dispatchers.IO) {
            ledgerRepository.countPerCategoryIdByUid(uid)
        }
    }

    suspend fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long {
        return withMDCContext(Dispatchers.IO) { ledgerRepository.countByCreatedAtBetween(startAt, endAt) }
    }
}
