package com.oksusu.susu.count.application

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.count.infrastructure.CountRepository
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO) {
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
