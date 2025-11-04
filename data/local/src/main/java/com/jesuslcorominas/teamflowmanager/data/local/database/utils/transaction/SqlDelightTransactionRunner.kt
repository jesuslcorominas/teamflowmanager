package com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction

import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SqlDelightTransactionRunner(private val executor: TransactionExecutor) :
    TransactionRunner {

    override suspend fun <T> run(block: suspend () -> T): T {
        try {
            return withContext(Dispatchers.IO) {
                executor.runInTransaction(block)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            throw e
        }
    }
}
