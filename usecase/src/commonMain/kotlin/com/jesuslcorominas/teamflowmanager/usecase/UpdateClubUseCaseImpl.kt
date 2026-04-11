package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateClubUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class UpdateClubUseCaseImpl(
    private val clubRepository: ClubRepository,
) : UpdateClubUseCase {
    override suspend fun invoke(
        id: String,
        name: String,
        homeGround: String?,
    ): Club = clubRepository.updateClub(id, name, homeGround)
}
