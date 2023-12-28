package com.oksusu.susu.event.model

import java.time.LocalDateTime

open class BaseEvent(
    val publishAt: LocalDateTime = LocalDateTime.now(),
)
