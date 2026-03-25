package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team

/**
 * Data class to hold the result of joining a club by invitation code.
 */
data class JoinClubResult(
    val club: Club,
    val orphanTeam: Team?,
    val clubMember: ClubMember,
)

interface JoinClubByCodeUseCase {
    /**
     * Join a club using an invitation code.
     * If the user has an orphan team (team without clubId), it will be linked to the club.
     * A clubMember document will be created with role "Coach" if an orphan team exists,
     * or "Miembro" if no orphan team exists.
     *
     * @param invitationCode The invitation code for the club
     * @return JoinClubResult containing the club, orphan team (if any), and club member
     * @throws IllegalArgumentException if invitation code is invalid
     * @throws IllegalStateException if user is not authenticated or club not found
     */
    suspend operator fun invoke(invitationCode: String): JoinClubResult
}
