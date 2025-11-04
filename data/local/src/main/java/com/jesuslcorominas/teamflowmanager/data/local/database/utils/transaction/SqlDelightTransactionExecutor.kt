package com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction

import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase

internal class SqlDelightTransactionExecutor(private val database: TeamFlowManagerDatabase) : TransactionExecutor {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.transactionWithResult {
            block()
        }
    }
}
