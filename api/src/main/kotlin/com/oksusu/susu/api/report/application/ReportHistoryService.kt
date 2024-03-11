package com.oksusu.susu.api.report.application

import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.report.domain.ReportHistory
import com.oksusu.susu.api.report.domain.vo.ReportTargetType
import com.oksusu.susu.api.report.infrastructure.ReportHistoryRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportHistoryService(
    private val reportHistoryRepository: ReportHistoryRepository,
) {
    suspend fun existsByUidAndTargetIdAndTargetType(
        uid: Long,
        targetId: Long,
        targetType: ReportTargetType,
    ): Boolean {
        return withMDCContext(Dispatchers.IO) {
            reportHistoryRepository.existsByUidAndTargetIdAndTargetType(
                uid = uid,
                targetId = targetId,
                targetType = targetType
            )
        }
    }

    @Transactional
    fun saveSync(reportHistory: ReportHistory): ReportHistory {
        return reportHistoryRepository.save(reportHistory)
    }
}
