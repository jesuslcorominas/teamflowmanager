package com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction

internal interface TransactionExecutor {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
