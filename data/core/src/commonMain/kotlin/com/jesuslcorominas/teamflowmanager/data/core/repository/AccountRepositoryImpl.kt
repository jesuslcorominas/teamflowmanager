package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AccountDeletionDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.AccountRepository

internal class AccountRepositoryImpl(
    private val accountDeletionDataSource: AccountDeletionDataSource,
) : AccountRepository {
    override suspend fun deleteAccount(): Result<Unit> = accountDeletionDataSource.deleteAccount()
}
