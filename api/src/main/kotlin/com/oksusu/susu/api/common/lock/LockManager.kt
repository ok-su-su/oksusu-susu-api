package com.oksusu.susu.api.common.lock

interface LockManager {
    suspend fun <T> lock(type: LockType, key: String, block: suspend () -> T): T
}
