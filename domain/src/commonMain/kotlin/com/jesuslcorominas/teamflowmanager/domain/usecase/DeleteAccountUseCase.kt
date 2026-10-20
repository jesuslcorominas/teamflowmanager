package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Deletes the signed-in user's account and every piece of data attached to it.
 *
 * Irreversible, and required by Google Play: an app that signs people in has to
 * offer them a way out from inside the app.
 */
interface DeleteAccountUseCase {
    suspend operator fun invoke(): Result<Unit>
}
