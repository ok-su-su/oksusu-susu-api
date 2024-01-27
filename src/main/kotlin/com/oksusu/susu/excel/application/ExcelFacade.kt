package com.oksusu.susu.excel.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.excel.model.ReceivedSheet
import com.oksusu.susu.excel.model.SentSheet
import com.oksusu.susu.excel.model.Sheet
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.dhatim.fastexcel.Workbook
import org.springframework.core.io.buffer.DefaultDataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class ExcelFacade(
    private val excelService: ExcelService,
    private val excelDataHelper: ExcelDataHelper,
) {
    val logger = KotlinLogging.logger { }

    companion object {
        const val PAGE_SIZE = 100
    }

    /** 엑셀 생성 */
    suspend fun getAllEnvelopsExcel(user: AuthUser): DefaultDataBuffer {
        val factory = DefaultDataBufferFactory()

        return withContext(Dispatchers.Default) {
            val (wb, os) = excelService.initWorkbook()

            val sent = async { createSheet(user.uid, wb, SentSheet.getSheet(), excelDataHelper.getSentData) }
            val received =
                async { createSheet(user.uid, wb, ReceivedSheet.getSheet(), excelDataHelper.getReceivedData) }
            awaitAll(sent, received)

            wb.finish()

            factory.wrap(os.toByteArray())
        }
    }

    suspend fun <T : Any> createSheet(
        uid: Long,
        wb: Workbook,
        sheet: Sheet,
        func: suspend (uid: Long, pageable: Pageable) -> Slice<T>,
    ) {
        val ws = excelService.initSheet(wb, sheet)

        var pageNum = 0
        do {
            val pageable = PageRequest.of(pageNum, PAGE_SIZE)

            val data = withContext(Dispatchers.IO) { func(uid, pageable) }

            excelService.insertData(
                ws = ws,
                datas = data.content,
                startIndex = pageNum * PAGE_SIZE,
                sheet = sheet
            )

            pageNum++
        } while (data.hasNext())
    }
}
