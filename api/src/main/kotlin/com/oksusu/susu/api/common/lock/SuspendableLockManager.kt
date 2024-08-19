package com.oksusu.susu.api.common.lock

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.FailToExecuteException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.springframework.stereotype.Component
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

private enum class LockReturn {
    /**
     * 락 실행
     */
    PROCESS_LOCK,

    /**
     * 락 해제됨
     */
    UNLOCK,

    /**
     * 등록된 채널 삭제
     */
    DELETE_CHANNEL,

    /**
     * 락 큐가 안비었음
     */
    NOT_EMPTY_QUEUE,

    /**
     * 락 큐가 비었음
     */
    EMPTY_QUEUE,
    ;
}

private sealed class LockMsg {
    /** 락 획득 시도 */
    class TryLock(val channel: SendChannel<LockReturn>) : LockMsg()

    /** 락 해제 */
    class UnLock(val channel: SendChannel<LockReturn>) : LockMsg()

    /** 등록된 채널 지우기 */
    class DeleteChannel(val channel: SendChannel<LockReturn>) : LockMsg()

    /** 큐 비었는지 확인 */
    class CheckQueueEmpty(val channel: SendChannel<LockReturn>) : LockMsg()
}

@OptIn(ObsoleteCoroutinesApi::class)
private fun lockActor() = CoroutineScope(Dispatchers.IO).actor<LockMsg>(capacity = 1000) {
    // queue 맨 앞 == 락 설정
    val lockQueue = LinkedList<SendChannel<LockReturn>>()

    for (msg in channel) {
        when (msg) {
            is LockMsg.TryLock -> {
                // 큐에 채널 등록하기
                lockQueue.offer(msg.channel)

                // 만약 방금 등록한 채널이 큐의 맨 앞이라면 바로 실행
                if (lockQueue.peek() == msg.channel) {
                    msg.channel.send(LockReturn.PROCESS_LOCK)
                }
            }

            is LockMsg.UnLock -> {
                // 현재 락을 획득한 채널을 큐에서 삭제
                lockQueue.poll()

                // 다음 락 획득 대상 notify하기
                if (lockQueue.peek() != null) {
                    lockQueue.peek().send(LockReturn.PROCESS_LOCK)
                }

                // 락 해제 및 큐 삭제 완료 알리기
                msg.channel.send(LockReturn.UNLOCK)
            }

            is LockMsg.DeleteChannel -> {
                if (lockQueue.peek() == msg.channel) {
                    // 삭제하려는 채널이 큐의 맨 앞일 때, 큐에서 삭제하고 다음꺼 실행
                    lockQueue.poll()
                    if (lockQueue.peek() != null) {
                        lockQueue.peek().send(LockReturn.PROCESS_LOCK)
                    }
                } else {
                    // 삭제하려는 채널이 큐의 맨 앞이 아닐 때, 큐에서만 삭제
                    lockQueue.remove(msg.channel)
                }

                // 삭제 완료 처리 알리기
                msg.channel.send(LockReturn.DELETE_CHANNEL)
            }

            is LockMsg.CheckQueueEmpty -> {
                if (lockQueue.peek() == null) {
                    msg.channel.send(LockReturn.EMPTY_QUEUE)
                } else {
                    msg.channel.send(LockReturn.NOT_EMPTY_QUEUE)
                }
            }
        }
    }
}

@Component
class SuspendableLockManager : LockManager {
    companion object {
        private const val WAIT_TIME = 3000L
        private const val LEASE_TIME = 3000L
    }

    private val actorMap = ConcurrentHashMap<String, SendChannel<LockMsg>>()

    override suspend fun <T> lock(key: String, block: suspend () -> T): T {
        // lock 관련 리턴 받을 채널
        Channel<LockReturn>().run {
            val channel = this

            // 락 설정
            val actor = tryLock(key, channel)

            try {
                // 로직 실행
                return withTimeout(LEASE_TIME) {
                    block()
                }
            } catch (e: TimeoutCancellationException) {
                // 락 보유 시간 에러 처리
                throw FailToExecuteException(ErrorCode.LOCK_TIMEOUT_ERROR)
            } catch (e: Exception) {
                // 나머지 에러 처리
                throw e
            } finally {
                // 락 해제
                releaseLock(actor, channel)

                // 큐가 빈 액터 삭제
                deleteEmptyQueueActor(channel, key)

                logger.info { actorMap }
            }
        }
    }

    private suspend fun tryLock(key: String, channel: Channel<LockReturn>): SendChannel<LockMsg> {
        val actor = actorMap.compute(key) { _, value ->
            val actor = value ?: lockActor()

            runBlocking(Dispatchers.Unconfined) {
                actor.send(LockMsg.TryLock(channel))
            }

            actor
        } ?: throw FailToExecuteException(ErrorCode.FAIL_TO_GET_LOCK)

        try {
            withTimeout(WAIT_TIME) {
                channel.receive()
            }
        } catch (e: TimeoutCancellationException) {
            // 락 획득 시간 에러 처리
            throw FailToExecuteException(ErrorCode.ACQUIRE_LOCK_TIMEOUT)
        } catch (e: Exception) {
            // 수신 채널 지우기
            actor.send(LockMsg.DeleteChannel(channel))
            channel.receive()

            throw FailToExecuteException(ErrorCode.FAIL_TO_EXECUTE_LOCK)
        }

        return actor
    }

    private suspend fun releaseLock(actor: SendChannel<LockMsg>, channel: Channel<LockReturn>) {
        actor.send(LockMsg.UnLock(channel))
        channel.receive()
    }

    private suspend fun deleteEmptyQueueActor(channel: Channel<LockReturn>, key: String) {
        actorMap.computeIfPresent(key) { _, value ->
            val rtn = runBlocking(Dispatchers.Unconfined) {
                value.send(LockMsg.CheckQueueEmpty(channel))
                channel.receive()
            }

            if (rtn == LockReturn.EMPTY_QUEUE) {
                null
            } else {
                value
            }
        }
    }
}
