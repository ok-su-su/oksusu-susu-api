package com.oksusu.susu.api.excel.model

import com.oksusu.susu.api.category.model.CategoryModel
import com.oksusu.susu.api.friend.model.RelationshipModel
import com.oksusu.susu.common.extension.format
import com.oksusu.susu.common.extension.toOX
import com.oksusu.susu.domain.envelope.infrastructure.model.EnvelopeDetailAndLedgerModel

data class AllEnvelopeSheetDataDto(
    val date: String,
    val categoryName: String,
    val relationship: String,
    val friendName: String,
    val amount: Long,
    val hasVisited: String?,
    val gift: String?,
    val memo: String?,
    val phoneNumber: String?,
) {
    companion object {
        fun receivedDto(
            model: EnvelopeDetailAndLedgerModel,
            category: CategoryModel,
            relationship: RelationshipModel,
        ): AllEnvelopeSheetDataDto {
            return AllEnvelopeSheetDataDto(
                date = model.envelope.handedOverAt.format("yyyy-MM-dd"),
                categoryName = model.ledger!!.title,
                relationship = relationship.relation,
                friendName = model.friend.name,
                amount = model.envelope.amount,
                hasVisited = model.envelope.hasVisited?.toOX(),
                gift = model.envelope.gift,
                memo = model.envelope.memo,
                phoneNumber = model.friend.phoneNumber
            )
        }

        fun sentDto(
            model: EnvelopeDetailAndLedgerModel,
            category: CategoryModel,
            relationship: RelationshipModel,
        ): AllEnvelopeSheetDataDto {
            return AllEnvelopeSheetDataDto(
                date = model.envelope.handedOverAt.format("yyyy-MM-dd"),
                categoryName = category.name,
                relationship = relationship.relation,
                friendName = model.friend.name,
                amount = model.envelope.amount,
                hasVisited = model.envelope.hasVisited?.toOX(),
                gift = model.envelope.gift,
                memo = model.envelope.memo,
                phoneNumber = model.friend.phoneNumber
            )
        }
    }
}
