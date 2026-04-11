package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.RegenerateInvitationCodeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class RegenerateInvitationCodeUseCaseImpl(
    private val clubRepository: ClubRepository,
) : RegenerateInvitationCodeUseCase {
    override suspend fun invoke(id: String): String = clubRepository.regenerateInvitationCode(id)
}
