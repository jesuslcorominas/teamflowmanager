package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository

/**
 * Use case to check if there is local data (team) without an associated user ID.
 * This is useful for detecting data created before user authentication was added.
 */
interface HasLocalDataWithoutUserIdUseCase {
    suspend operator fun invoke(): Boolean
}

internal class HasLocalDataWithoutUserIdUseCaseImpl(
    private val teamRepository: TeamRepository,
) : HasLocalDataWithoutUserIdUseCase {
    override suspend fun invoke(): Boolean = teamRepository.hasLocalTeamWithoutUserId()
}
