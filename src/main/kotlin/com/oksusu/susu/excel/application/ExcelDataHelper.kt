package com.oksusu.susu.excel.application

import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.excel.model.AllEnvelopeSheetDataDto
import com.oksusu.susu.friend.application.RelationshipService
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component

@Component
class ExcelDataHelper(
    private val envelopeService: EnvelopeService,
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
) {
    val getReceivedData: suspend (uid: Long, pageable: Pageable) -> Slice<AllEnvelopeSheetDataDto> = { uid, pageable ->
        envelopeService.getDetailAndLedgersByEnvelopeType(
            uid = uid,
            envelopeType = EnvelopeType.RECEIVED,
            pageable = pageable
        ).map { model ->
            AllEnvelopeSheetDataDto.receivedDto(
                model = model,
                category = categoryService.getCategory(model.categoryAssignment.categoryId),
                relationship = relationshipService.getRelationship(model.friendRelationship.relationshipId)
            )
        }
    }

    val getSentData: suspend (uid: Long, pageable: Pageable) -> Slice<AllEnvelopeSheetDataDto> = { uid, pageable ->
        envelopeService.getDetailAndLedgersByEnvelopeType(
            uid = uid,
            envelopeType = EnvelopeType.SENT,
            pageable = pageable
        ).map { model ->
            AllEnvelopeSheetDataDto.sentDto(
                model = model,
                category = categoryService.getCategory(model.categoryAssignment.categoryId),
                relationship = relationshipService.getRelationship(model.friendRelationship.relationshipId)
            )
        }
    }
}
