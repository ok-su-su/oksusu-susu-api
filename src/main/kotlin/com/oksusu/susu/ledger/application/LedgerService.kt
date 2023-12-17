package com.oksusu.susu.ledger.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.infrastructure.LedgerRepository
import com.oksusu.susu.ledger.model.request.CreateLedgerRequest
import com.oksusu.susu.ledger.model.response.CreateLedgerResponse
import com.oksusu.susu.ledger.model.response.SearchLedgerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LedgerService(
    private val ledgerRepository: LedgerRepository
) {
    suspend fun create(user: AuthUser, request: CreateLedgerRequest): CreateLedgerResponse {
        val createdLedger = Ledger(
            uid = user.id,
            title = request.title,
            description = request.description
        ).run { saveSync(this) }

        return CreateLedgerResponse.from(createdLedger)
    }

    @Transactional
    fun saveSync(ledger: Ledger): Ledger {
        return ledgerRepository.save(ledger)
    }

    suspend fun getAll(user: AuthUser, pageRequest: SusuPageRequest): Page<SearchLedgerResponse> {
        return withContext(Dispatchers.IO) {
            ledgerRepository.findAllByUid(user.id, pageRequest.toDefault())
        }.map { ledger -> SearchLedgerResponse.from(ledger) }
    }

    @Transactional
    suspend fun delete(user: AuthUser, ids: Set<Long>) {
        // ids validate 추가 필요, 사이즈 체크
        withContext(Dispatchers.IO) {
            ledgerRepository.deleteAllByIdInBatch(ids)
        }
    }
}
