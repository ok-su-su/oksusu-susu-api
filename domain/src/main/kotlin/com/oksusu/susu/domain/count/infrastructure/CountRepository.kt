package com.oksusu.susu.domain.count.infrastructure

import com.oksusu.susu.domain.count.domain.Count
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface CountRepository : JpaRepository<Count, Long> {
    fun findByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType): Count?

    fun deleteByTargetIdAndTargetType(targetId: Long, targetType: CountTargetType)

    fun deleteAllByTargetTypeAndTargetIdIn(targetType: CountTargetType, targetIds: List<Long>)
}
