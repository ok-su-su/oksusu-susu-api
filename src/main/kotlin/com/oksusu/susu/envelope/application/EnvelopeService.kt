package com.oksusu.susu.envelope.application

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.envelope.infrastructure.model.CountTotalAmountsAndCountsModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeDetailModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EnvelopeService(
    private val envelopeRepository: EnvelopeRepository,
) {
    @Transactional
    fun saveSync(envelope: Envelope): Envelope {
        return envelopeRepository.save(envelope)
    }

    @Transactional
    fun deleteSync(envelope: Envelope) {
        envelopeRepository.delete(envelope)
    }

    @Transactional
    fun deleteAllByLedgerId(ledgerId: Long) {
        envelopeRepository.deleteAllByLedgerId(ledgerId)
    }

    suspend fun findAllByLedgerId(ledgerId: Long): List<Envelope> {
        return withContext(Dispatchers.IO) { envelopeRepository.findAllByLedgerId(ledgerId) }
    }

    suspend fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel> {
        return withContext(Dispatchers.IO) { envelopeRepository.countTotalAmountsAndCounts(ledgerIds) }
    }

    suspend fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel {
        return withContext(Dispatchers.IO) { envelopeRepository.countTotalAmountAndCount(ledgerId) }
    }

    suspend fun findByIdOrThrow(id: Long, uid: Long): Envelope {
        return findByIdOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)
    }

    suspend fun findByIdOrNull(id: Long, uid: Long): Envelope? {
        return withContext(Dispatchers.IO) { envelopeRepository.findByIdAndUid(id, uid) }
    }

    suspend fun getDetail(id: Long, uid: Long): EnvelopeDetailModel {
        return withContext(Dispatchers.IO) {
            envelopeRepository.findDetailEnvelope(id, uid)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)
    }
}
