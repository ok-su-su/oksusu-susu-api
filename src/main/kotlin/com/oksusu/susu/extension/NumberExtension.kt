package com.oksusu.susu.extension

import java.time.LocalDate

fun Long.toAgeGroup(): Long {
    val ages = listOf<Long>(0, 10, 20, 30, 40, 50, 60, 70, 80, 90)
    val idx = LocalDate.now().year - this + 1
    if (idx > 9) {
        return ages[9]
    }
    return ages[idx.toInt()]
}

fun Int.toYearMonth(): String {
    val strNum = this.toString()
    val year = strNum.substring(0, 4)
    val month = strNum.substring(4, 6)

    return "$year.$month"
}
