package com.oksusu.susu.envelope.application

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.envelope.infrastructure.model.CountPerHandedOverAtModel
import com.oksusu.susu.envelope.infrastructure.model.CountTotalAmountsAndCountsModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeAndFriendModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeDetailAndLedgerModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeDetailModel
import com.oksusu.susu.envelope.infrastructure.model.FriendStatisticsModel
import com.oksusu.susu.envelope.infrastructure.model.SearchEnvelopeSpec
import com.oksusu.susu.envelope.infrastructure.model.SearchFriendStatisticsSpec
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class EnvelopeService(
    private val envelopeRepository: EnvelopeRepository,
) {
    val logger = KotlinLogging.logger { }

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

    suspend fun getDetailAndLedgersByEnvelopeType(
        uid: Long,
        envelopeType: EnvelopeType,
        pageable: Pageable,
    ): Slice<EnvelopeDetailAndLedgerModel> {
        return withContext(Dispatchers.IO) {
            envelopeRepository.findAllDetailEnvelopeAndLedgerByEnvelopeType(uid, envelopeType, pageable)
        }
    }

    suspend fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel? {
        return withContext(Dispatchers.IO) {
            envelopeRepository.getMaxAmountEnvelopeInfoByUid(uid, type)
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

    suspend fun countAvgAmountPerStatisticGroup(): List<CountAvgAmountPerStatisticGroupModel> {
        return withContext(Dispatchers.IO) {
            envelopeRepository.countAvgAmountPerStatisticGroup()
        }
    }

    suspend fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<Envelope> {
        return withContext(Dispatchers.IO) { envelopeRepository.search(spec, pageable) }
    }

    suspend fun findFriendStatistics(
        searchSpec: SearchFriendStatisticsSpec,
        pageable: Pageable,
    ): Page<FriendStatisticsModel> {
        return withContext(Dispatchers.IO) { envelopeRepository.findFriendStatistics(searchSpec, pageable) }
    }

    suspend fun findLatestFriendEnvelopes(friendIds: Set<Long>): List<Envelope> {
        return withContext(Dispatchers.IO) { envelopeRepository.findLatestFriendEnvelopes(friendIds) }
    }

    suspend fun findTop1ByUidAndTypeOrderByAmountAsc(uid: Long, type: EnvelopeType): Envelope? {
        return withContext(Dispatchers.IO) { envelopeRepository.findTop1ByUidAndTypeOrderByAmountAsc(uid, type) }
    }

    suspend fun findTop1ByUidAndTypeOrderByAmountDesc(uid: Long, type: EnvelopeType): Envelope? {
        return withContext(Dispatchers.IO) { envelopeRepository.findTop1ByUidAndTypeOrderByAmountDesc(uid, type) }
    }

    suspend fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long {
        return withContext(Dispatchers.IO) { envelopeRepository.countByCreatedAtBetween(startAt, endAt) }
    }

    suspend fun count(): Long {
        return withContext(Dispatchers.IO) { envelopeRepository.count() }
    }
}
