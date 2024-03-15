package com.oksusu.susu.common.extension

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CancellationException

fun KLogger.resolveCancellation(name: String, throwable: Throwable, other: (KLogger.() -> Unit)? = null) {
    when (throwable) {
        is CancellationException -> debug { "[$name] Job was cancelled." }
        else -> {
            other?.let { it() } ?: error(throwable) { "$name Job Error ${throwable.message}" }
        }
    }
}
