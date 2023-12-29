package com.oksusu.susu.envelope.application

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.envelope.infrastructure.model.*
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class EnvelopeService(
    private val envelopeRepository: EnvelopeRepository,
) {
    val logger = mu.KotlinLogging.logger { }

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

    suspend fun getMaxAmountByUidAndTypeByUid(uid: Long, type: EnvelopeType): Long? {
        return withContext(Dispatchers.IO) {
            envelopeRepository.getMaxAmountByUid(uid, type)
        }
    }

    suspend fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel? {
        return getMaxAmountByUidAndTypeByUid(uid, type)?.let { maxAmount ->
            withContext(Dispatchers.IO) {
                envelopeRepository.findEnvelopeAndFriendByUid(maxAmount, uid, type)
            }
        }
    }

    suspend fun countPerHandedOverAtInLast8Month(type: EnvelopeType): List<CountPerHandedOverAtModel> {
        val from = LocalDate.now().minusMonths(7).atTime(0, 0)
        val to = LocalDate.now().atTime(23, 59)
        return withContext(Dispatchers.IO) {
            envelopeRepository.countPerHandedOverAtBetween(type, from, to)
        }
    }

    suspend fun countPerHandedOverAtInLast8MonthByUid(uid: Long, type: EnvelopeType): List<CountPerHandedOverAtModel> {
        val from = LocalDate.now().minusMonths(7).atTime(0, 0)
        val to = LocalDate.now().atTime(23, 59)
        return withContext(Dispatchers.IO) {
            envelopeRepository.countPerHandedOverAtBetweenByUid(uid, type, from, to)
        }
    }

    suspend fun countPerCategoryId(): List<CountPerCategoryIdModel> {
        return withContext(Dispatchers.IO) {
            envelopeRepository.countPerCategoryId()
        }
    }

    suspend fun countPerCategoryIdByUid(
        uid: Long,
    ): List<CountPerCategoryIdModel> {
        return withContext(Dispatchers.IO) {
            envelopeRepository.countPerCategoryIdByUid(uid)
        }
    }

    suspend fun countAvgAmountPerCategoryIdAndRelationshipIdAndBirth(): List<CountAvgAmountPerCategoryIdAndRelationshipIdAndBirthModel> {
        return withContext(Dispatchers.IO) {
            envelopeRepository.countAvgAmountPerCategoryIdAndRelationshipIdAndBirth()
        }
    }
}
