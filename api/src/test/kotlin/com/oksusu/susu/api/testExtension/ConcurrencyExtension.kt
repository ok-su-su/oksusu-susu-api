package com.oksusu.susu.api.testExtension

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger { }

const val THREAD_COUNT = 50
const val CONCURRENT_COUNT = 1000

suspend fun <T> executeConcurrency(successCount: AtomicLong, block: suspend () -> T?) {
    val executorService = Executors.newFixedThreadPool(THREAD_COUNT)
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
