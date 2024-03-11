package com.oksusu.susu.api.count.infrastructure

import com.oksusu.susu.api.count.domain.Count
import com.oksusu.susu.api.count.domain.vo.CountTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CountRepository : JpaRepository<Count, Long> {
    @Transactional(readOnly = true)
    fun findByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType): Count?

    @Transactional
    fun deleteByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType)

    @Transactional
    fun deleteAllByTargetTypeAndTargetIdIn(targetType: CountTargetType, targetIds: List<Long>)
}
