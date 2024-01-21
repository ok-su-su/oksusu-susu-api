package com.oksusu.susu.count.infrastructure

import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.domain.vo.CountTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CountRepository : JpaRepository<Count, Long> {
    @Transactional
    fun findByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType): Count

    fun findByTargetTypeAndTargetIdIn(targetType: CountTargetType, targetIds: List<Long>): List<Count>
}
