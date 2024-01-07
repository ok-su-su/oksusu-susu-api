package com.oksusu.susu.excel.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.excel.model.SheetDataDto
import com.oksusu.susu.excel.model.SheetType
import com.oksusu.susu.friend.application.RelationshipService
import kotlinx.coroutines.*
import org.dhatim.fastexcel.Workbook
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class ExcelFacade(
    private val excelService: ExcelService,
    private val envelopeService: EnvelopeService,
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
) {
    val logger = mu.KotlinLogging.logger { }

    companion object {
        const val PAGE_SIZE = 1000
    }

    /** 엑셀 생성 */
    suspend fun getExcel(user: AuthUser): DefaultDataBuffer {
        val factory = DefaultDataBufferFactory()

        return withContext(Dispatchers.Default) {
            val os = ByteArrayOutputStream()

            val wb = excelService.initWorkbook(os)

            val sent = async { createSentSheet(user.id, wb) }
            val received = async { createReceivedSheet(user.id, wb) }
            awaitAll(sent, received)

            wb.finish()

            factory.wrap(os.toByteArray())
        }
    }

    /** 받아요 sheet */
    private suspend fun createReceivedSheet(uid: Long, wb: Workbook) {
        val ws = excelService.initSheet(wb, SheetType.RECEIVED)

        var pageNum = 0
        do {
            val pageable = PageRequest.of(pageNum, PAGE_SIZE)

            val detailAndLedgerModels = envelopeService.getDetailAndLedgersByEnvelopeType(
                uid = uid,
                envelopeType = EnvelopeType.RECEIVED,
                pageable = pageable
            ).map { model ->
                SheetDataDto.receivedDto(
                    model = model,
                    category = categoryService.getCategory(model.categoryAssignment.categoryId),
                    relationship = relationshipService.getRelationship(model.friendRelationship.relationshipId)
                )
            }

            excelService.insertData(ws, detailAndLedgerModels.content, pageNum)

            pageNum++
        } while (detailAndLedgerModels.hasNext())
    }

    /** 보내요 sheet */
    private suspend fun createSentSheet(uid: Long, wb: Workbook) {
        val ws = excelService.initSheet(wb, SheetType.SENT)

        var pageNum = 0
        do {
            val pageable = PageRequest.of(pageNum, PAGE_SIZE)

            val detailAndLedgerModels = envelopeService.getDetailAndLedgersByEnvelopeType(
                uid = uid,
                envelopeType = EnvelopeType.SENT,
                pageable = pageable
            ).map { model ->
                SheetDataDto.sentDto(
                    model = model,
                    category = categoryService.getCategory(model.categoryAssignment.categoryId),
                    relationship = relationshipService.getRelationship(model.friendRelationship.relationshipId)
                )
            }

            excelService.insertData(ws, detailAndLedgerModels.content, pageNum)

            pageNum++
        } while (detailAndLedgerModels.hasNext())
    }
}
