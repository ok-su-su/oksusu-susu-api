package com.oksusu.susu.api.common.lock

import com.oksusu.susu.api.testExtension.CONCURRENT_COUNT
import com.oksusu.susu.api.testExtension.coExecuteConcurrency
import com.oksusu.susu.api.testExtension.executeConcurrency
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class SuspendableLockManagerTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val lockManager = SuspendableLockManager()
    val countService1 = CountService()
    val countService2 = CountService()
    val countService3 = CountService()

    beforeEach {
        countService1.apply { this.counter = 0 }
        countService2.apply { this.counter = 0 }
        countService3.apply { this.counter = 0 }
    }

    describe("suspendable lock manager") {
        context("락을 설정하면") {
            it("여러 코루틴으로 동작했을 때, 카운트가 동작한 수만큼 증가해야한다.") {
                val successCount = AtomicLong()

                coExecuteConcurrency(successCount) {
                    lockManager.lock("1") {
                        countService1.increase()
                        logger.info { "1 ${countService1.counter}" }
                    }
                }

                countService1.counter shouldBeEqual CONCURRENT_COUNT
                successCount.get() shouldBeEqual CONCURRENT_COUNT.toLong()
            }

            it("여러 쓰레드를 생성해 동작했을 때, 카운트가 동작한 수만큼 증가해야한다.") {
                val successCount = AtomicLong()

                executeConcurrency(successCount) {
                    lockManager.lock("1") {
                        countService1.increase()
                        logger.info { "1 ${countService1.counter}" }
                    }
                }

                countService1.counter shouldBeEqual CONCURRENT_COUNT
                successCount.get() shouldBeEqual CONCURRENT_COUNT.toLong()
            }

            it("여러 코루틴으로 동작했을 때, 키 별로 락이 지정되고, 카운트가 올바르게 증가해야한다.") {
                val successCount = AtomicLong()

                coroutineScope {
                    val deferreds = mutableListOf<Deferred<Long>>()
                    for (i in 1..CONCURRENT_COUNT) {
                        val deferred1 = async(Dispatchers.IO) {
                            lockManager.lock("1") {
                                countService1.increase()
                                logger.info { "1 ${countService1.counter}" }
                            }
                            successCount.getAndIncrement()
                        }
                        val deferred2 = async(Dispatchers.IO) {
                            lockManager.lock("2") {
                                countService2.increase()
                                logger.info { "2 ${countService2.counter}" }
                            }
                            successCount.getAndIncrement()
                        }
                        val deferred3 = async(Dispatchers.IO) {
                            lockManager.lock("3") {
                                countService3.increase()
                                logger.info { "3 ${countService3.counter}" }
                            }
                            successCount.getAndIncrement()
                        }

                        deferreds.add(deferred1)
                        deferreds.add(deferred2)
                        deferreds.add(deferred3)
                    }

                    awaitAll(*deferreds.toTypedArray())
                }

                countService1.counter shouldBeEqual CONCURRENT_COUNT
                countService2.counter shouldBeEqual CONCURRENT_COUNT
                countService3.counter shouldBeEqual CONCURRENT_COUNT
                successCount.get() shouldBeEqual CONCURRENT_COUNT * 3L
            }

            it("여러 쓰레드를 생성해 동작했을 때, 키 별로 락이 지정되고, 카운트가 올바르게 증가해야한다.") {
                val successCount = AtomicLong()
                val executorService = Executors.newFixedThreadPool(30)
                val latch = CountDownLatch(30)
                for (i in 1..CONCURRENT_COUNT) {
                    executorService.submit {
                        try {
                            runBlocking {
                                lockManager.lock("1") {
                                    countService1.increase()
                                    logger.info { "1 ${countService1.counter}" }
                                }
                            }
                            successCount.getAndIncrement()
                        } catch (e: Throwable) {
                            logger.info { e.toString() }
                        } finally {
                            latch.countDown()
                        }
                    }
                    executorService.submit {
                        try {
                            runBlocking {
                                lockManager.lock("2") {
                                    countService2.increase()
                                    logger.info { "2 ${countService2.counter}" }
                                }
                            }
                            successCount.getAndIncrement()
                        } catch (e: Throwable) {
                            logger.info { e.toString() }
                        } finally {
                            latch.countDown()
                        }
                    }
                    executorService.submit {
                        try {
                            runBlocking {
                                lockManager.lock("3") {
                                    countService3.increase()
                                    logger.info { "3 ${countService3.counter}" }
                                }
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

                countService1.counter shouldBeEqual CONCURRENT_COUNT
                countService2.counter shouldBeEqual CONCURRENT_COUNT
                countService3.counter shouldBeEqual CONCURRENT_COUNT
                successCount.get() shouldBeEqual CONCURRENT_COUNT * 3L
            }
        }
    }
})

private class CountService {
    var counter: Int = 0

    suspend fun increase() {
        val curCount = counter
        delay(100)
        counter = curCount + 1
    }
}
