package com.oksusu.susu.excel.model

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeDetailAndLedgerModel
import com.oksusu.susu.extension.format
import com.oksusu.susu.extension.toOX
import com.oksusu.susu.friend.model.RelationshipModel

class AllEnvelopeSheetDataDto(
    val date: String,
    val categoryName: String,
    val relationship: String,
    val friendName: String,
    val amount: Long,
    val hasVisited: String,
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
                hasVisited = model.envelope.hasVisited.toOX(),
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
                hasVisited = model.envelope.hasVisited.toOX(),
                gift = model.envelope.gift,
                memo = model.envelope.memo,
                phoneNumber = model.friend.phoneNumber
            )
        }
    }
}
