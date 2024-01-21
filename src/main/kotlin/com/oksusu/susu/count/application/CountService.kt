package com.oksusu.susu.count.application

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.infrastructure.CountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CountService(
    private val countRepository: CountRepository,
) {
    @Transactional
    fun saveSync(count: Count): Count {
        return countRepository.save(count)
    }

    @Transactional
    fun saveAllSync(counts: List<Count>): List<Count> {
        return countRepository.saveAll(counts)
    }
}