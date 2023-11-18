package com.oksusu.susu.extension

import kotlinx.coroutines.CancellationException
import mu.KLogger

fun KLogger.resolveCancellation(name: String, throwable: Throwable, other: (KLogger.() -> Unit)? = null) {
    when (throwable) {
        is CancellationException -> debug { "[$name] Job was cancelled." }
        else -> {
            other?.let { it() } ?: error(throwable) { "$name Job Error ${throwable.message}" }
        }
    }
}
