package com.oksusu.susu.extension

import java.time.LocalDate

fun Long.toStatisticAgeGroup(): Long {
    val ages = listOf<Long>(0, 10, 20, 30, 40, 50, 60, 70)
    val idx = (LocalDate.now().year - this + 1) / 10
    if (idx > 7) {
        return ages[7]
    }
    return ages[idx.toInt()]
}

fun Int.toYearMonth(): String {
    val strNum = this.toString()
    val year = strNum.substring(0, 4)
    val month = strNum.substring(4, 6)

    return "$year.$month"
}
