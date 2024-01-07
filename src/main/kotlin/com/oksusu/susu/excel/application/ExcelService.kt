package com.oksusu.susu.excel.application

import com.oksusu.susu.excel.model.SheetDataDto
import com.oksusu.susu.excel.model.SheetType
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class ExcelService {
    companion object {
        val titles = listOf(
            "",
            "날짜",
            "경조사",
            "나와의 관계",
            "이름",
            "금액",
            "방문여부",
            "선물",
            "메모",
            "연락처"
        )
    }

    /** 제목 입력 */
    suspend fun insertTitle(ws: Worksheet, sheetType: SheetType) {
        ws.range(0, 0, 0, titles.size - 1).merge()

        ws.value(0, 0, sheetType.kr)

        titles.forEachIndexed { idx, title ->
            ws.value(1, idx, title)
            styleColumnForTitle(ws, 0, idx)
            styleColumnForTitle(ws, 1, idx)
        }
    }

    /** 스타일 적용 */
    suspend fun initWorkBookStyle(wb: Workbook) {
        wb.setGlobalDefaultFont("Arial", 11.0)
    }

    suspend fun initWorkSheetStyle(ws: Worksheet) {
        titles.forEachIndexed { columnIdx, _ ->
            ws.style(columnIdx)
                .horizontalAlignment("center")
                .verticalAlignment("center")
                .set()
            ws.width(columnIdx, 15.0)
        }
    }

    suspend fun styleColumnForTitle(ws: Worksheet, r: Int, c: Int) {
        ws.style(r, c).borderStyle(BorderStyle.THIN)
            .horizontalAlignment("center")
            .verticalAlignment("center")
            .fillColor("F8D58C")
            .set()
    }

    suspend fun styleColumnForContent(ws: Worksheet, r: Int, c: Int) {
        ws.style(r, c).borderStyle(BorderStyle.THIN)
            .horizontalAlignment("center")
            .verticalAlignment("center")
            .set()
    }

    /** 한 줄 입력 */
    suspend fun insertData(ws: Worksheet, datas: List<SheetDataDto>, pageNum: Int) {
        datas.forEachIndexed { idx, data ->
            val r = pageNum + idx + 2
            val hasVisited = if (data.hasVisited) {
                "O"
            } else {
                "X"
            }

            ws.value(r, 0, idx + 1)
            ws.value(r, 1, data.date)
            ws.style(r, 1).format("yyyy.MM.dd").set()
            ws.value(r, 2, data.categoryName)
            ws.value(r, 3, data.relationship)
            ws.value(r, 4, data.friendName)
            ws.value(r, 5, data.amount)
            ws.value(r, 6, hasVisited)
            ws.value(r, 7, data.gift)
            ws.value(r, 8, data.memo)
            ws.value(r, 9, data.phoneNumber)
            for (c in 0..9) {
                styleColumnForContent(ws, idx + 2, c)
            }
        }
    }

    /** sheet 생성 및 초기 세팅 */
    suspend fun initSheet(wb: Workbook, sheetType: SheetType): Worksheet {
        val ws = wb.newWorksheet(sheetType.kr)
        initWorkSheetStyle(ws)
        insertTitle(ws, sheetType)
        return ws
    }

    /** workbook 생성 및 초기 세팅 */
    suspend fun initWorkbook(os: ByteArrayOutputStream): Workbook {
        val wb = Workbook(os, "excel", "1.0")
        initWorkBookStyle(wb)
        return wb
    }
}
