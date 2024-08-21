package com.oksusu.susu.api.common.lock

import com.oksusu.susu.api.config.LockConfig
import com.oksusu.susu.api.testExtension.CONCURRENT_COUNT
import com.oksusu.susu.api.testExtension.THREAD_COUNT
import com.oksusu.susu.api.testExtension.executeConcurrency
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

class SuspendableLockManagerTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockCoroutineExceptionHandler = mockk<ErrorPublishingCoroutineExceptionHandler>()
    val mockLockConfig = mockk<LockConfig.ActorLockConfig>()

    every { mockCoroutineExceptionHandler.handler } returns CoroutineExceptionHandler { _, _ -> }
    every { mockLockConfig.waitTimeMilli } returns 1000
    every { mockLockConfig.leaseTimeMilli } returns 3000


    val lockManager = SuspendableLockManager(mockCoroutineExceptionHandler, mockLockConfig)
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
            it("여러 쓰레드를 생성해 동작했을 때, 카운트가 동작한 수만큼 증가해야한다.") {
                val successCount = AtomicLong()

                executeConcurrency(successCount) {
                    lockManager.lock("1") {
                        countService1.increase()
                        logger.info { "1 ${countService1.counter}" }
                    }
                }

                lockManager.clearEmptyActor()

                countService1.counter shouldBeEqual CONCURRENT_COUNT
                successCount.get() shouldBeEqual CONCURRENT_COUNT.toLong()
            }

            it("여러 쓰레드를 생성해 동작했을 때, 앞선 요청 처리에 오랜 시간이 걸린다면 에러가 발생해야한다.") {
                val successCount = AtomicLong()

                executeConcurrency(
                    successCount = successCount,
                    concurrentCount = 500
                ) {
                    lockManager.lock("1") {
                        countService1.increaseWithDelay500()
                        logger.info { "1 ${countService1.counter}" }
                    }
                }

                lockManager.clearEmptyActor()

                countService1.counter shouldBeLessThan CONCURRENT_COUNT
                successCount.get() shouldBeLessThan CONCURRENT_COUNT.toLong()
            }

            it("여러 쓰레드를 생성해 동작했을 때, 요청 처리에 시간이 락 획득 시간보다 오래 걸린다면 에러가 발생해야한다.") {
                val successCount = AtomicLong()

                executeConcurrency(
                    successCount = successCount,
                    concurrentCount = 500
                ) {
                    lockManager.lock("1") {
                        countService1.increaseWithDelay3000()
                        logger.info { "1 ${countService1.counter}" }
                    }
                }

                lockManager.clearEmptyActor()

                countService1.counter shouldBeEqual 0
                successCount.get() shouldBeEqual 0
            }

            it("여러 쓰레드를 생성해 동작했을 때, 키 별로 락이 지정되고, 카운트가 올바르게 증가해야한다.") {
                val successCount = AtomicLong()
                val executorService = Executors.newFixedThreadPool(THREAD_COUNT * 3)
                val latch = CountDownLatch(CONCURRENT_COUNT * 3)
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
                    executorService.submit {
                        runBlocking {
                            lockManager.clearEmptyActor()
                        }
                    }
                }
                latch.await()

                lockManager.clearEmptyActor()

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

    fun increase() {
        val curCount = counter
        counter = curCount + 1
    }

    suspend fun increaseWithDelay500() {
        val curCount = counter
        delay(500)
        counter = curCount + 1
    }

    suspend fun increaseWithDelay3000() {
        val curCount = counter
        delay(3000)
        counter = curCount + 1
    }
}
