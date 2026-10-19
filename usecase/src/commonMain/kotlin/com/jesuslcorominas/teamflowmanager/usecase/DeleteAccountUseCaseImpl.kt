package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteAccountUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AccountRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import kotlinx.coroutines.flow.first

internal class DeleteAccountUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
    private val deleteFcmTokenUseCase: DeleteFcmTokenUseCase,
) : DeleteAccountUseCase {
    override suspend fun invoke(): Result<Unit> {
        val user =
            authRepository.getCurrentUser().first()
                ?: return Result.failure(IllegalStateException("No signed-in user to delete"))

        // Unsubscribing this device from its club topic has to happen here: the
        // server deletes the token document, but only the client can tell FCM to
        // stop delivering to the device. Best effort — a device that cannot be
        // unsubscribed is no reason to refuse the deletion, and the topic becomes
        // undeliverable anyway once the account is gone.
        runCatching { deleteFcmTokenUseCase(user.id) }

        return accountRepository
            .deleteAccount()
            // The auth record is already gone server-side; this clears the cached
            // session so the app stops acting as a user that no longer exists.
            .onSuccess { authRepository.signOut() }
    }
}
