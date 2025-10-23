package com.jesuslcorominas.teamflowmanager.domain.utils

interface TransactionRunner {
    suspend fun run(block: suspend () -> Unit)
}
