package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AccountRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteAccountUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var accountRepository: AccountRepository
    private lateinit var deleteFcmTokenUseCase: DeleteFcmTokenUseCase
    private lateinit var useCase: DeleteAccountUseCaseImpl

    private val user = User(id = "user123", email = "a@b.com", displayName = "A", photoUrl = null)

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        accountRepository = mockk(relaxed = true)
        deleteFcmTokenUseCase = mockk(relaxed = true)
        useCase = DeleteAccountUseCaseImpl(authRepository, accountRepository, deleteFcmTokenUseCase)
    }

    @Test
    fun `deletes the fcm token, then the account, then signs out`() =
        runTest {
            every { authRepository.getCurrentUser() } returns flowOf(user)
            coEvery { accountRepository.deleteAccount() } returns Result.success(Unit)

            val result = useCase()

            assertTrue(result.isSuccess)
            coVerifyOrder {
                deleteFcmTokenUseCase("user123")
                accountRepository.deleteAccount()
                authRepository.signOut()
            }
        }

    @Test
    fun `when nobody is signed in, fails without touching the account`() =
        runTest {
            every { authRepository.getCurrentUser() } returns flowOf(null)

            val result = useCase()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            coVerify(exactly = 0) { accountRepository.deleteAccount() }
            coVerify(exactly = 0) { deleteFcmTokenUseCase(any()) }
            coVerify(exactly = 0) { authRepository.signOut() }
        }

    @Test
    fun `when the cascade fails, does not sign out`() =
        runTest {
            val error = IllegalStateException("deleteAccount returned 500")
            every { authRepository.getCurrentUser() } returns flowOf(user)
            coEvery { accountRepository.deleteAccount() } returns Result.failure(error)

            val result = useCase()

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { authRepository.signOut() }
        }

    @Test
    fun `when unsubscribing the device fails, the account is still deleted`() =
        runTest {
            every { authRepository.getCurrentUser() } returns flowOf(user)
            coEvery { deleteFcmTokenUseCase("user123") } throws RuntimeException("FCM unreachable")
            coEvery { accountRepository.deleteAccount() } returns Result.success(Unit)

            val result = useCase()

            assertTrue(result.isSuccess)
            coVerify { accountRepository.deleteAccount() }
            coVerify { authRepository.signOut() }
        }
}
