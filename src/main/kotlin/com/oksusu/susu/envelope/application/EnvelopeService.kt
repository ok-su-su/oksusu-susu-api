package com.oksusu.susu.envelope.application

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.envelope.infrastructure.model.CountTotalAmountsAndCountsModel
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

    suspend fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel> {
        return withContext(Dispatchers.IO) { envelopeRepository.countTotalAmountsAndCounts(ledgerIds) }
    }
}
