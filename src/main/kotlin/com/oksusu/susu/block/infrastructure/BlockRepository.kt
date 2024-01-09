package com.oksusu.susu.block.infrastructure

import com.oksusu.susu.block.domain.Block
import com.oksusu.susu.block.domain.vo.BlockTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface BlockRepository : JpaRepository<Block, Long> {
    @Transactional(readOnly = true)
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: BlockTargetType): Boolean
}
