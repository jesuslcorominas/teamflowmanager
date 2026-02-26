package com.jesuslcorominas.teamflowmanager.data.remote.transaction

import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner

/**
 * TransactionRunner implementation for Firestore.
 * The actual Firestore transaction coordination is handled within each DataSource operation.
 * This runner wraps the block execution for atomic operation tracking.
 */
class FirestoreTransactionRunner : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}
