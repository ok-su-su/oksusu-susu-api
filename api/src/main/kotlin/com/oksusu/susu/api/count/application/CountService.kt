package com.oksusu.susu.api.count.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.count.domain.Count
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.count.infrastructure.CountRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CountService(
    private val countRepository: CountRepository,
) {
    val logger = KotlinLogging.logger {}

    @Transactional
    fun saveSync(count: Count): Count {
        return countRepository.save(count)
    }

    @Transactional
    fun saveAllSync(counts: List<Count>): List<Count> {
        return countRepository.saveAll(counts)
    }

    suspend fun findByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType): Count {
        return withMDCContext(Dispatchers.IO) {
            countRepository.findByTargetIdAndTargetType(targetId, targetType)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_COUNT_ERROR)
    }

    @Transactional
    fun deleteByTargetIdAndTargetType(id: Long, type: CountTargetType) {
        countRepository.deleteByTargetIdAndTargetType(id, type)
    }

    @Transactional
    fun deleteAllByTargetTypeAndTargetIdIn(type: CountTargetType, targetIds: List<Long>) {
        countRepository.deleteAllByTargetTypeAndTargetIdIn(type, targetIds)
    }
}
