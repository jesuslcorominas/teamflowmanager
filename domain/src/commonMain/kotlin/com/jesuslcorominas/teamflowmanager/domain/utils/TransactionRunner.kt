package com.jesuslcorominas.teamflowmanager.domain.utils

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}
