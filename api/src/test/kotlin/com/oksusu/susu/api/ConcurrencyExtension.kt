package com.oksusu.susu.api

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger { }

suspend fun <T> executeConcurrency(successCount: AtomicLong, block: suspend () -> T?) {
    coroutineScope {
        val deferreds = mutableListOf<Deferred<Unit>>()
        for (i in 1..5) {
            val deferred = async(Dispatchers.IO) {
                try {
                    block()
                    successCount.getAndIncrement()
                } catch (e: Exception) {
                    logger.error { e }
                }

                return@async
            }

            deferreds.add(deferred)
        }

        awaitAll(*deferreds.toTypedArray())
    }
}
