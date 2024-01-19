package com.oksusu.susu.excel.application

import com.oksusu.susu.excel.model.Sheet
import com.oksusu.susu.extension.getPropertyValues
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class ExcelService {
    val logger = mu.KotlinLogging.logger { }

    /** 제목 입력 */
    fun insertTitle(ws: Worksheet, sheet: Sheet) {
        ws.range(0, 0, 0, sheet.titles.size).merge()

        ws.value(0, 0, sheet.name)
        styleColumnForTitle(ws, 0, 0)
        styleColumnForTitle(ws, 1, 0)

        sheet.titles.keys.forEachIndexed { idx, title ->
            val c = idx + 1

            ws.value(1, c, title)
            styleColumnForTitle(ws, 0, c)
            styleColumnForTitle(ws, 1, c)
        }
    }

    /** 데이터 입력 */
    fun <T : Any> insertData(ws: Worksheet, datas: List<T>, startIndex: Int, sheet: Sheet) {
        datas.forEachIndexed { dataIdx, data ->
            val r = startIndex + dataIdx + 2

            val properties = data.getPropertyValues()

            ws.value(r, 0, dataIdx + 1)
            sheet.titles.values.forEachIndexed { propertyIdx, name ->
                ws.value(r, propertyIdx + 1, properties[name])
            }

            for (c in 0..properties.size) {
                styleColumnForContent(ws, dataIdx + 2, c)
            }
        }
    }

    /** sheet 생성 및 초기 세팅 */
    fun initSheet(wb: Workbook, sheet: Sheet): Worksheet {
        val ws = wb.newWorksheet(sheet.name)
        initWorkSheetStyle(ws, sheet)
        insertTitle(ws, sheet)
        return ws
    }

    /** workbook 생성 및 초기 세팅 */
    fun initWorkbook(): Pair<Workbook, ByteArrayOutputStream> {
        val os = ByteArrayOutputStream()
        val wb = Workbook(os, "excel", "1.0")
        initWorkBookStyle(wb)
        return wb to os
    }

    /** 스타일 적용 */
    fun initWorkBookStyle(wb: Workbook) {
        wb.setGlobalDefaultFont("Arial", 11.0)
    }

    fun initWorkSheetStyle(ws: Worksheet, sheet: Sheet) {
        sheet.titles.keys.forEachIndexed { columnIdx, _ ->
            ws.style(columnIdx)
                .horizontalAlignment("center")
                .verticalAlignment("center")
                .set()
            ws.width(columnIdx, 15.0)
        }
    }

    fun styleColumnForTitle(ws: Worksheet, r: Int, c: Int) {
        ws.style(r, c).borderStyle(BorderStyle.THIN)
            .horizontalAlignment("center")
            .verticalAlignment("center")
            .fillColor("F8D58C")
            .set()
    }

    fun styleColumnForContent(ws: Worksheet, r: Int, c: Int) {
        ws.style(r, c).borderStyle(BorderStyle.THIN)
            .horizontalAlignment("center")
            .verticalAlignment("center")
            .set()
    }
}
