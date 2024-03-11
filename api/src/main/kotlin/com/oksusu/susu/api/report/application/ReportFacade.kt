package com.oksusu.susu.api.report.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.common.exception.AlreadyException
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.extension.coExecute
import com.oksusu.susu.api.post.application.PostService
import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.oksusu.susu.api.report.model.request.ReportCreateRequest
import com.oksusu.susu.api.report.model.response.ReportCreateResponse
import com.oksusu.susu.api.report.model.response.ReportMetadataResponse
import com.oksusu.susu.api.user.application.UserService
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
        if (user.uid == request.targetId) {
            throw InvalidRequestException(ErrorCode.INVALID_REPORT_ERROR)
        }

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

        if (!isExists && request.targetType == ReportTargetType.POST) {
            throw NotFoundException(ErrorCode.NOT_FOUND_POST_ERROR)
        }

        if (!isExists && request.targetType == ReportTargetType.USER) {
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
