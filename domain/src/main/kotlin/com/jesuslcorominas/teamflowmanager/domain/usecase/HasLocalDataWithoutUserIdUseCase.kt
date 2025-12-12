package com.jesuslcorominas.teamflowmanager.domain.usecase
/**
 * Use case to check if there is local data (team) without an associated user ID.
 * This is useful for detecting data created before user authentication was added.
 */
interface HasLocalDataWithoutUserIdUseCase {
    suspend operator fun invoke(): Boolean
}
