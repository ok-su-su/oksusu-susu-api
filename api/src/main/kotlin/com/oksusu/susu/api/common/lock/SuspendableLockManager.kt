package com.oksusu.susu.api.common.lock

import com.oksusu.susu.api.config.LockConfig
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.FailToExecuteException
import com.oksusu.susu.common.extension.milliToSec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

private sealed class LockMsg {
    /** 락 설정 */
    class Lock(
        val requestTime: LocalDateTime,
        val block: suspend () -> Any?,
        val result: CompletableDeferred<Any?>,
    ) : LockMsg()

    /** 엑터의 채널이 비었는지 확인 */
    class IsEmpty(
        val result: CompletableDeferred<Boolean>,
    ) : LockMsg()
}

/**
 * 특정 키에 해당된 작업을 순차적으로 수행하는 actor
 * msg가 실행된다 == 락을 획득했다 로 여김
 */
@OptIn(ObsoleteCoroutinesApi::class, ExperimentalCoroutinesApi::class)
private fun lockActor(
    waitTimeMilli: Long,
    leaseTimeMilli: Long,
) = CoroutineScope(Dispatchers.IO).actor<LockMsg>(capacity = 1000) {
    for (msg in channel) {
        when (msg) {
            is LockMsg.Lock -> {
                if (msg.requestTime.isBefore(LocalDateTime.now().minusSeconds(waitTimeMilli.milliToSec()))) {
                    // 락은 획득했지만 락 획득 시간보다 더 오랜 시간이 걸렸다면 timeout 에러 발생
                    msg.result.complete(FailToExecuteException(ErrorCode.ACQUIRE_LOCK_TIMEOUT))
                } else {
                    try {
                        // 로직 실행 및 deferred에 결과값 넣기
                        withTimeout(leaseTimeMilli) {
                            val rtn = msg.block()

                            msg.result.complete(rtn)
                        }
                    } catch (e: TimeoutCancellationException) {
                        // 락 획득 시간 에러 처리
                        msg.result.complete(FailToExecuteException(ErrorCode.LOCK_TIMEOUT_ERROR))
                    } catch (e: Exception) {
                        // 이외의 에러 처리
                        msg.result.complete(e)
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
    /** 락 시도 */
    class TryLock(
        val requestTime: LocalDateTime = LocalDateTime.now(),
        val key: String,
        val block: suspend () -> Any?,
        val result: CompletableDeferred<Any?>,
    ) : LockManagerMsg()

    /** 미사용 엑터 삭제 */
    class ClearActor : LockManagerMsg()
}

/**
 * 키에 해당하는 actor에 요청을 보내는 역할을 하는 actor
 */
@OptIn(ObsoleteCoroutinesApi::class)
private fun lockManagerActor(
    waitTimeMilli: Long,
    leaseTimeMilli: Long,
) = CoroutineScope(Dispatchers.IO).actor<LockManagerMsg>(capacity = 1000) {
    val actorMap = HashMap<String, SendChannel<LockMsg>>()

    for (msg in channel) {
        when (msg) {
            is LockManagerMsg.TryLock -> {
                if (msg.requestTime.isBefore(LocalDateTime.now().minusSeconds(waitTimeMilli.milliToSec()))) {
                    // 락 획득 시간보다 더 오랜 시간이 걸렸다면 timeout 에러 발생
                    msg.result.complete(FailToExecuteException(ErrorCode.ACQUIRE_LOCK_TIMEOUT))
                } else {
                    try {
                        // actor 가져오기
                        val actor = actorMap.computeIfAbsent(msg.key) { _ ->
                            lockActor(
                                waitTimeMilli = waitTimeMilli,
                                leaseTimeMilli = leaseTimeMilli
                            )
                        }

                        // 락 처리 요청
                        actor.send(
                            LockMsg.Lock(
                                requestTime = LocalDateTime.now(),
                                block = msg.block,
                                result = msg.result
                            )
                        )
                    } catch (e: Exception) {
                        msg.result.complete(e)
                    }
                }
            }

            is LockManagerMsg.ClearActor -> {
                logger.info { "before $actorMap" }

                // actor channel 비었는지 조회
                actorMap.entries.chunked(100).map { actors ->
                    val deferreds = actors.map { (key, actor) ->
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
                                // 빔
                                key
                            } else {
                                // 안빔
                                ""
                            }
                        }
                    }.toTypedArray()

                    // 빈 actor만 필터링
                    val emptyKeys = awaitAll(*deferreds).filter { it != "" }

                    // 빈 actor 삭제
                    emptyKeys.forEach { key -> actorMap.remove(key) }
                }

                logger.info { "after $actorMap" }
            }
        }
    }
}

/**
 * render 안하면 잘 보입니다. render 푸세요
 *
 * SuspendableLockManager -> LockManagerActor -> LockActor </br>
 *              ^                   |               |
 *              |-----------------------------------
 *
 * SuspendableLockManager -> LockManagerActor -> LockActor 순서로 메세지가 전달됨
 *
 * Deferred에 응답을 채우는 방식으로 LockManagerActor / LockActor 에서 SuspendableLockManager 로 응답이 전달됨
 * 에러 또한 이 방식으로 전달됨
 */
@Component
class SuspendableLockManager(
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
    private val lockConfig: LockConfig.ActorLockConfig,
) : LockManager {
    private val actor = lockManagerActor(
        waitTimeMilli = lockConfig.waitTimeMilli,
        leaseTimeMilli = lockConfig.leaseTimeMilli
    )

    override suspend fun <RETURN> lock(key: String, block: suspend () -> RETURN): RETURN {
        val result = CompletableDeferred<Any?>()

        // 락 시도
        actor.send(
            LockManagerMsg.TryLock(
                key = key,
                block = block,
                result = result
            )
        )

        return try {
            // 서비스 로직 반환값 or 에러
            val rtn = withTimeout(lockConfig.waitTimeMilli + lockConfig.leaseTimeMilli + 1000) {
                result.await()
            }

            // 반환값이 에러면 throw
            if (rtn is Exception) {
                throw rtn
            }

            // TODO: as RETURN 안하는 방법 찾아서 수정 바람
            rtn as RETURN
        } catch (e: TimeoutCancellationException) {
            // 락 획득 시간 에러 처리
            throw FailToExecuteException(ErrorCode.LOCK_TIMEOUT_ERROR)
        } catch (e: Exception) {
            // 이외의 에러 처리
            throw e
        }
    }

    /**
     * channel이 빈 엑터 map에서 삭제하는 스케줄러
     */
    @Scheduled(fixedDelay = 1000 * 60)
    private fun scheduledClearEmptyActor() {
        CoroutineScope(Dispatchers.IO + coroutineExceptionHandler.handler).launch {
            clearEmptyActor()
        }
    }

    /**
     * channel이 빈 엑터 map에서 삭제
     */
    suspend fun clearEmptyActor() {
        actor.send(LockManagerMsg.ClearActor())
    }
}
