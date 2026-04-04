package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.RemoveClubMemberUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import kotlinx.coroutines.flow.first

internal class RemoveClubMemberUseCaseImpl(
    private val clubMemberRepository: ClubMemberRepository,
    private val teamRepository: TeamRepository,
) : RemoveClubMemberUseCase {
    override suspend fun invoke(
        userId: String,
        clubId: String,
    ) {
        val teams = teamRepository.getTeamsByClub(clubId).first()
        val coachTeam = teams.find { it.coachId == userId }
        if (coachTeam != null) {
            val teamFirestoreId = coachTeam.firestoreId
            requireNotNull(teamFirestoreId) { "Team firestoreId is null" }
            teamRepository.clearTeamCoach(teamFirestoreId)
        }
        clubMemberRepository.removeClubMember(userId, clubId)
    }
}
