package com.oksusu.susu.extension

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object Zone {
    val KST: ZoneId = ZoneId.of("Asia/Seoul")
    val UTC: ZoneId = ZoneId.of("UTC")
}

fun LocalDateTime.toInstant(): Instant {
    return this.toInstant(ZoneOffset.of("+09:00"))
}

fun LocalDateTime.equalsFromYearToSec(otherTime: LocalDateTime): Boolean {
    return this.year == otherTime.year &&
        this.month == otherTime.month &&
        this.dayOfMonth == otherTime.dayOfMonth &&
        this.hour == otherTime.hour &&
        this.minute == otherTime.minute &&
        this.second == otherTime.second
}
