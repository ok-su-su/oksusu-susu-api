package com.oksusu.susu.api.testExtension

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger { }

const val CONCURRENT_COUNT = 10

suspend fun <T> coExecuteConcurrency(successCount: AtomicLong, block: suspend () -> T?) {
    coroutineScope {
        val deferreds = mutableListOf<Deferred<Unit>>()
        for (i in 1..CONCURRENT_COUNT) {
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

suspend fun <T> executeConcurrency(successCount: AtomicLong, block: suspend () -> T?) {
    val executorService = Executors.newFixedThreadPool(CONCURRENT_COUNT)
    val latch = CountDownLatch(CONCURRENT_COUNT)
    for (i in 1..CONCURRENT_COUNT) {
        executorService.submit {
            try {
                runBlocking {
                    block()
                }
                successCount.getAndIncrement()
            } catch (e: Throwable) {
                logger.info { e.toString() }
            } finally {
                latch.countDown()
            }
        }
    }
    latch.await()
}
