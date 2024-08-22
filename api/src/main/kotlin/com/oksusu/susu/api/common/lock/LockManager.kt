package com.oksusu.susu.api.common.lock

interface LockManager {
    /**
     * 서비스 로직 (block)에 key를 이용하여 락을 설정한 후 실행한다.
     */
    suspend fun <T> lock(key: String, block: suspend () -> T): T
}
