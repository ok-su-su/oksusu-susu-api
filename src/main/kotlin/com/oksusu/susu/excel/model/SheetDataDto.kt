package com.oksusu.susu.excel.model

import com.oksusu.susu.category.model.CategoryModel
import com.oksusu.susu.envelope.infrastructure.model.EnvelopeDetailAndLedgerModel
import com.oksusu.susu.friend.model.RelationshipModel
import java.time.LocalDateTime

class SheetDataDto(
    val date: LocalDateTime,
    val categoryName: String,
    val relationship: String,
    val friendName: String,
    val amount: Long,
    val hasVisited: Boolean,
    val gift: String?,
    val memo: String?,
    val phoneNumber: String?,
) {
    companion object {
        fun receivedDto(
            model: EnvelopeDetailAndLedgerModel,
            category: CategoryModel,
            relationship: RelationshipModel,
        ): SheetDataDto {
            return SheetDataDto(
                date = model.envelope.handedOverAt,
                categoryName = model.ledger!!.title,
                relationship = relationship.relation,
                friendName = model.friend.name,
                amount = model.envelope.amount,
                hasVisited = model.envelope.hasVisited,
                gift = model.envelope.gift,
                memo = model.envelope.memo,
                phoneNumber = model.friend.phoneNumber
            )
        }

        fun sentDto(
            model: EnvelopeDetailAndLedgerModel,
            category: CategoryModel,
            relationship: RelationshipModel,
        ): SheetDataDto {
            return SheetDataDto(
                date = model.envelope.handedOverAt,
                categoryName = category.name,
                relationship = relationship.relation,
                friendName = model.friend.name,
                amount = model.envelope.amount,
                hasVisited = model.envelope.hasVisited,
                gift = model.envelope.gift,
                memo = model.envelope.memo,
                phoneNumber = model.friend.phoneNumber
            )
        }
    }
}
