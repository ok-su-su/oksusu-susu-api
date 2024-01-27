package com.oksusu.susu.report.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.AlreadyException
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.post.application.PostService
import com.oksusu.susu.report.domain.ReportHistory
import com.oksusu.susu.report.domain.vo.ReportTargetType
import com.oksusu.susu.report.model.request.ReportCreateRequest
import com.oksusu.susu.report.model.response.ReportCreateResponse
import com.oksusu.susu.report.model.response.ReportMetadataResponse
import com.oksusu.susu.user.application.UserService
import org.springframework.stereotype.Service

@Service
class ReportFacade(
    private val reportMetadataService: ReportMetadataService,
    private val reportHistoryService: ReportHistoryService,
    private val postService: PostService,
    private val userService: UserService,
    private val txTemplates: TransactionTemplates,
) {
    suspend fun getAllMetadata(targetType: ReportTargetType): ReportMetadataResponse {
        return reportMetadataService.getAllTargetType(targetType)
            .run { ReportMetadataResponse(this) }
    }

    // TODO: 추후 수정 작업 진행
    suspend fun report(user: AuthUser, request: ReportCreateRequest): ReportCreateResponse {
        val metadata = reportMetadataService.get(request.metadataId)

        val isExistsReportHistory = reportHistoryService.existsByUidAndTargetIdAndTargetType(
            uid = user.uid,
            targetId = request.targetId,
            targetType = request.targetType
        )

        if (isExistsReportHistory) {
            throw AlreadyException(ErrorCode.ALREADY_EXISTS_REPORT_HISTORY_ERROR)
        }

        val isExists = when (request.targetType) {
            ReportTargetType.POST -> postService.existsById(request.targetId)
            ReportTargetType.USER -> userService.existsById(request.targetId)
        }

        if (isExists && request.targetType == ReportTargetType.POST) {
            throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
        }

        if (isExists && request.targetType == ReportTargetType.USER) {
            throw NotFoundException(ErrorCode.NOT_FOUND_USER_ERROR)
        }

        val createdReportHistory = txTemplates.writer.coExecute {
            ReportHistory(
                uid = user.uid,
                targetId = request.targetId,
                targetType = request.targetType,
                metadataId = metadata.id,
                description = request.description
            ).run { reportHistoryService.saveSync(this) }
        }

        return ReportCreateResponse(
            historyId = createdReportHistory.id,
            metadataId = createdReportHistory.metadataId
        )
    }
}
