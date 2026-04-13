package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class GetClubByIdUseCaseImpl(
    private val clubRepository: ClubRepository,
) : GetClubByIdUseCase {
    override suspend fun invoke(id: String): Club? = clubRepository.getClubById(id)
}
