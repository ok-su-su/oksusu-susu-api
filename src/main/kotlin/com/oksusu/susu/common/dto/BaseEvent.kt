package com.oksusu.susu.common.dto

import java.time.LocalDateTime

open class BaseEvent (
    val publishAt: LocalDateTime = LocalDateTime.now()
)