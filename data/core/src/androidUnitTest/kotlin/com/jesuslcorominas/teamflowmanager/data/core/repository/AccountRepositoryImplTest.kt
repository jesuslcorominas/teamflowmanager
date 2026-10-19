package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.AccountDeletionDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountRepositoryImplTest {
    private lateinit var accountDeletionDataSource: AccountDeletionDataSource
    private lateinit var repository: AccountRepositoryImpl

    @Before
    fun setup() {
        accountDeletionDataSource = mockk(relaxed = true)
        repository = AccountRepositoryImpl(accountDeletionDataSource)
    }

    @Test
    fun `delegates deletion to the datasource`() =
        runTest {
            coEvery { accountDeletionDataSource.deleteAccount() } returns Result.success(Unit)

            val result = repository.deleteAccount()

            assertTrue(result.isSuccess)
            coVerify { accountDeletionDataSource.deleteAccount() }
        }

    @Test
    fun `propagates the datasource failure untouched`() =
        runTest {
            val error = IllegalStateException("deleteAccount returned 500")
            coEvery { accountDeletionDataSource.deleteAccount() } returns Result.failure(error)

            val result = repository.deleteAccount()

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
