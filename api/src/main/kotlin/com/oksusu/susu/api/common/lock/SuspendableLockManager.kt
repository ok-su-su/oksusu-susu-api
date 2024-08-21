package com.oksusu.susu.api.common.lock

import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.FailToExecuteException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

private const val WAIT_TIME = 1000L
private const val LEASE_TIME = 3000L
private const val WAIT_TIME_SECONDS = WAIT_TIME * 1000
private const val LEASE_TIME_SECONDS = LEASE_TIME * 1000


private sealed class LockMsg {
    /** 락 획득 시도 */
    class Lock(
        val requestTime: LocalDateTime,
        val block: suspend () -> Any?,
        val blockResult: CompletableDeferred<Any?>,
    ) : LockMsg()

    class IsEmpty(
        val result: CompletableDeferred<Boolean>,
    ) : LockMsg()
}

@OptIn(ObsoleteCoroutinesApi::class)
private fun lockActor() = CoroutineScope(Dispatchers.IO).actor<LockMsg>(capacity = 1000) {
    for (msg in channel) {
        when (msg) {
            is LockMsg.Lock -> {
                if (msg.requestTime.isBefore(LocalDateTime.now().minusSeconds(WAIT_TIME_SECONDS))) {
                    msg.blockResult.complete(FailToExecuteException(ErrorCode.ACQUIRE_LOCK_TIMEOUT))
                } else {
                    try {
                        withTimeout(LEASE_TIME) {
                            val rtn = msg.block()

                            msg.blockResult.complete(rtn)
                        }
                    } catch (e: TimeoutCancellationException) {
                        // 락 획득 시간 에러 처리
                        msg.blockResult.complete(FailToExecuteException(ErrorCode.LOCK_TIMEOUT_ERROR))
                    } catch (e: Exception) {
                        // 이외의 에러 처리
                        msg.blockResult.complete(FailToExecuteException(ErrorCode.FAIL_TO_EXECUTE_LOCK))
                    }
                }
            }

            is LockMsg.IsEmpty -> {
                msg.result.complete(channel.isEmpty)
            }
        }
    }
}

private sealed class LockManagerMsg {
    /** 락 획득 시도 */
    class TryLock(
        val requestTime: LocalDateTime,
        val key: String,
        val block: suspend () -> Any?,
        val blockResult: CompletableDeferred<Any?>,
    ) : LockManagerMsg()

    class ClearActor : LockManagerMsg()
}

@OptIn(ObsoleteCoroutinesApi::class)
private fun lockManagerActor() = CoroutineScope(Dispatchers.IO).actor<LockManagerMsg>(capacity = 1000) {
    val actorMap = HashMap<String, SendChannel<LockMsg>>()

    for (msg in channel) {
        when (msg) {
            is LockManagerMsg.TryLock -> {
                logger.info { "${msg.requestTime} ${LocalDateTime.now()}" }
                if (msg.requestTime.isBefore(LocalDateTime.now().minusSeconds(WAIT_TIME_SECONDS))) {
                    msg.blockResult.complete(FailToExecuteException(ErrorCode.ACQUIRE_LOCK_TIMEOUT))
                } else {
                    try {
                        val actor = actorMap.computeIfAbsent(msg.key) { _ -> lockActor() }

                        actor.send(
                            LockMsg.Lock(
                                requestTime = LocalDateTime.now(),
                                block = msg.block,
                                blockResult = msg.blockResult,
                            )
                        )
                    } catch (e: Exception) {
                        msg.blockResult.complete(FailToExecuteException(ErrorCode.FAIL_TO_GET_LOCK))
                    }
                }
            }

            is LockManagerMsg.ClearActor -> {
                logger.info { "before ${actorMap}" }

                val deferreds = actorMap.entries.map { (key, actor) ->
                    async {
                        val result = CompletableDeferred<Boolean>()

                        actor.send(LockMsg.IsEmpty(result))

                        val isEmpty = try {
                            withTimeout(10) {
                                result.await()
                            }
                        } catch (e: Exception) {
                            // 에러에 대한 처리는 따로 하지않음
                            // 시간 안에 처리 안되면, 작업이 남았다고 간주
                            false
                        }

                        if (isEmpty) {
                            key
                        } else {
                            ""
                        }
                    }
                }.toTypedArray()

                val emptyKeys = awaitAll(*deferreds).filter { it != "" }

                emptyKeys.forEach { key -> actorMap.remove(key) }

                logger.info { "after ${actorMap}" }
            }
        }
    }
}

@Component
class SuspendableLockManager(
    val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
) : LockManager {

    private val actor = lockManagerActor()

    override suspend fun <T> lock(key: String, block: suspend () -> T): T {
        val result = CompletableDeferred<Any?>()
        val now = LocalDateTime.now()

        actor.send(
            LockManagerMsg.TryLock(
                requestTime = now,
                key = key, block = block,
                blockResult = result
            )
        )

        return try {
            val rtn = result.await()

            if (rtn is Exception) {
                throw rtn
            }

            rtn as T
        } catch (e: TimeoutCancellationException) {
            // 락 획득 시간 에러 처리
            throw FailToExecuteException(ErrorCode.LOCK_TIMEOUT_ERROR)
        } catch (e: Exception) {
            // 이외의 에러 처리
            throw e
        }

    }

    @Scheduled(fixedDelay = 1000 * 60)
    private fun scheduledClearEmptyActor() =
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch { clearEmptyActor() }

    suspend fun clearEmptyActor() {
        actor.send(LockManagerMsg.ClearActor())
    }
}
