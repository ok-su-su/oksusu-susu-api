package com.oksusu.susu.api.common.lock

interface LockManager {
    suspend fun <T> lock(key: String, block: () -> T): T
}
