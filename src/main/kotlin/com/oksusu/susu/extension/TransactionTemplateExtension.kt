package com.oksusu.susu.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

suspend fun <RETURN> TransactionTemplate.executeWithContext(
    actions: TransactionCallback<RETURN>,
): RETURN? {
    val transactionTemplate: TransactionTemplate = this

    return withContext(Dispatchers.IO) {
        transactionTemplate.execute(actions)
    }
}
