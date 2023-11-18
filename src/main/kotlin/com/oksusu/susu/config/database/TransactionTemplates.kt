package com.oksusu.susu.config.database

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW
import org.springframework.transaction.support.TransactionTemplate

@Component
class TransactionTemplates(transactionManager: PlatformTransactionManager) {
    val writer = TransactionTemplate(transactionManager)
    val newTxWriter = TransactionTemplate(transactionManager).apply { propagationBehavior = PROPAGATION_REQUIRES_NEW }
    val reader = TransactionTemplate(transactionManager).apply { isReadOnly = true }
}
