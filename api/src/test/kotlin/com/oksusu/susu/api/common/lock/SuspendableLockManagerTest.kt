package com.oksusu.susu.api.common.lock

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

class SuspendableLockManagerTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val lockManager = SuspendableLockManager()
    val countService1 = CountService()
    val countService2 = CountService()
    val countService3 = CountService()

    beforeEach {
        countService1.set(0)
        countService2.set(0)
        countService3.set(0)
    }

    describe("suspendable lock manager") {
        context("조회시") {
            it("올바른 config 값이 조회되어야 한다.") {
                val successCount = AtomicLong()

                executeConcurrency(successCount) {
                    lockManager.lock("1") {
                        val counter = countService1.counter
                        Thread.sleep(100)
                        logger.info { "1 $counter" }
                        countService1.set(counter + 1)
                    }
                }

                countService1.counter shouldBeEqual 5
                successCount.get() shouldBeEqual 5
            }

            it("올바른 config 값이 조회되어야 한다.") {
                val successCount = AtomicLong()

                coroutineScope {
                    val deferreds = mutableListOf<Deferred<Long>>()
                    for (i in 1..5) {
                        val deferred1 = async(Dispatchers.IO) {
                            lockManager.lock("1") {
                                val counter = countService1.counter
                                Thread.sleep(100)
                                logger.info { "1 $counter" }
                                countService1.set(counter + 1)
                            }
                            successCount.getAndIncrement()
                        }
                        val deferred2 = async(Dispatchers.IO) {
                            lockManager.lock("2") {
                                val counter = countService2.counter
                                Thread.sleep(100)
                                logger.info { "2 $counter" }
                                countService2.set(counter + 1)
                            }
                            successCount.getAndIncrement()
                        }
                        val deferred3 = async(Dispatchers.IO) {
                            lockManager.lock("3") {
                                val counter = countService3.counter
                                Thread.sleep(100)
                                logger.info { "3 $counter" }
                                countService3.set(counter + 1)
                            }
                            successCount.getAndIncrement()
                        }

                        deferreds.add(deferred1)
                        deferreds.add(deferred2)
                        deferreds.add(deferred3)
                    }

                    awaitAll(*deferreds.toTypedArray())
                }

                countService1.counter shouldBeEqual 5
                countService2.counter shouldBeEqual 5
                countService3.counter shouldBeEqual 5
                successCount.get() shouldBeEqual 15
            }
        }
    }
})

private class CountService {
    var counter: Int = 0

    fun set(a: Int) {
        counter = a
    }
}

private suspend fun <T> executeConcurrency(successCount: AtomicLong, block: suspend () -> T?) {
    coroutineScope {
        val deferreds = mutableListOf<Deferred<Long>>()
        for (i in 1..5) {
            val deferred = async(Dispatchers.IO) {
                block()
                successCount.getAndIncrement()
            }

            deferreds.add(deferred)
        }

        awaitAll(*deferreds.toTypedArray())
    }
}
