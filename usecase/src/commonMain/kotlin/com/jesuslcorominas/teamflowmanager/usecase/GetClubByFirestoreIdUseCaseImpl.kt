package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubByFirestoreIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository

internal class GetClubByFirestoreIdUseCaseImpl(
    private val clubRepository: ClubRepository,
) : GetClubByFirestoreIdUseCase {
    override suspend fun invoke(firestoreId: String): Club? = clubRepository.getClubByFirestoreId(firestoreId)
}
