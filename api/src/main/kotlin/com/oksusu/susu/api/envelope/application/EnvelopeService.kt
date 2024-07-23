package com.oksusu.susu.api.envelope.application

import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class EnvelopeService(
    private val envelopeRepository: EnvelopeRepository,
    private val statisticConfig: SusuConfig.StatisticConfig,
) {
    private val logger = KotlinLogging.logger { }

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
        return withMDCContext(Dispatchers.IO) { envelopeRepository.findAllByLedgerId(ledgerId) }
    }

    suspend fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel> {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.countTotalAmountsAndCounts(ledgerIds) }
    }

    suspend fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel {
        return withMDCContext(Dispatchers.IO) { countTotalAmountAndCountSync(ledgerId) }
    }

    fun countTotalAmountAndCountSync(ledgerId: Long): CountTotalAmountsAndCountsModel {
        return envelopeRepository.countTotalAmountAndCount(ledgerId)
    }

    suspend fun findByIdOrThrow(id: Long, uid: Long): Envelope {
        return findByIdOrNull(id, uid) ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)
    }

    suspend fun findByIdOrNull(id: Long, uid: Long): Envelope? {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.findByIdAndUid(id, uid) }
    }

    suspend fun getDetail(id: Long, uid: Long): EnvelopeDetailModel {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.findDetailEnvelope(id, uid)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_ENVELOPE_ERROR)
    }

    suspend fun getDetailAndLedgersByEnvelopeType(
        uid: Long,
        envelopeType: EnvelopeType,
        pageable: Pageable,
    ): Slice<EnvelopeDetailAndLedgerModel> {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.findAllDetailEnvelopeAndLedgerByEnvelopeType(uid, envelopeType, pageable)
        }
    }

    suspend fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel? {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.getMaxAmountEnvelopeInfoByUid(uid, type)
        }
    }

    suspend fun getTotalAmountPerHandedOverAtInLast1YearByUid(
        uid: Long,
        type: EnvelopeType,
    ): List<CountPerHandedOverAtModel> {
        val from = LocalDate.now().minusMonths(11).atTime(0, 0)
        val to = LocalDate.now().atTime(23, 59)
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.getTotalAmountPerHandedOverAtBetweenByUid(uid, type, from, to)
        }
    }

    suspend fun countPerCategoryIdByUid(
        uid: Long,
    ): List<CountPerCategoryIdModel> {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.countPerCategoryIdByUid(uid) }
    }

    suspend fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<Envelope> {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.search(spec, pageable) }
    }

    suspend fun findFriendStatistics(
        searchSpec: SearchFriendStatisticsSpec,
        pageable: Pageable,
    ): Page<FriendStatisticsModel> {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.findFriendStatistics(searchSpec, pageable)
        }
    }

    suspend fun findLatestFriendEnvelopes(friendIds: Set<Long>): List<Envelope> {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.findLatestFriendEnvelopes(friendIds) }
    }

    suspend fun findTop1ByUidAndTypeOrderByAmountAsc(uid: Long, type: EnvelopeType): Envelope? {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.findTop1ByUidAndTypeOrderByAmountAsc(uid, type)
        }
    }

    suspend fun findTop1ByUidAndTypeOrderByAmountDesc(uid: Long, type: EnvelopeType): Envelope? {
        return withMDCContext(Dispatchers.IO) {
            envelopeRepository.findTop1ByUidAndTypeOrderByAmountDesc(uid, type)
        }
    }

    suspend fun count(): Long {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.count() }
    }

    suspend fun countTotalAmountByUid(uid: Long): Long? {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.countTotalAmountByUid(uid) }
    }

    @Transactional
    fun deleteAllByFriendIds(friendIds: List<Long>) {
        envelopeRepository.deleteAllByFriendIdIn(friendIds)
    }

    suspend fun countByUidAndFriendId(uid: Long, friendId: Long): Long {
        return withMDCContext(Dispatchers.IO) { envelopeRepository.countByUidAndFriendId(uid, friendId) }
    }
}
