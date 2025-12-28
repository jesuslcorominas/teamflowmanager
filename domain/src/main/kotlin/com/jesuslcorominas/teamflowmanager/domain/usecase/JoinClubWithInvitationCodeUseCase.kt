package com.jesuslcorominas.teamflowmanager.domain.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.Team

/**
 * Use case to join a club with an invitation code.
 * This will:
 * 1. Find the club by invitation code
 * 2. Detect orphan teams owned by the user
 * 3. Link the orphan team to the club
 * 4. Create/update the club member with "Coach" role
 *
 * Note: This implementation uses Firebase caching and error handling to ensure
 * data consistency. If any operation fails, the error is thrown to allow the caller
 * to handle it appropriately.
 */
interface JoinClubWithInvitationCodeUseCase {
    /**
     * Join a club using an invitation code.
     * @param invitationCode The invitation code for the club
     * @return Result containing the updated team and club member if successful, or error otherwise
     */
    suspend operator fun invoke(invitationCode: String): Result<JoinClubResult>

    data class JoinClubResult(
        val team: Team,
        val clubMember: ClubMember,
        val clubName: String
    )

    class ClubNotFoundException(message: String) : Exception(message)
    class NoOrphanTeamsException(message: String) : Exception(message)
    class TeamUpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)
    class ClubMemberUpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
