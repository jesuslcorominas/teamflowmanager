package com.jesuslcorominas.teamflowmanager.data.local.database.utils.transaction

import androidx.room.withTransaction
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase

internal interface TransactionExecutor {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

internal class RoomTransactionExecutor (private val database: TeamFlowManagerDatabase) : TransactionExecutor {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        if (database.inTransaction()) block() else database.withTransaction(block)
}
