package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Use case that sends a push notification to a coach when a president assigns them to a team.
 * Self-assignments (where the assignor and assignee are the same user) must NOT trigger a notification.
 */
interface NotifyCoachAssignedOnTeamAssignmentUseCase {
    /**
     * Notifies [coachUserId] that they have been assigned to [teamName] by [assignedByUserId].
     * If [assignedByUserId] equals [coachUserId] (self-assignment), no notification is sent.
     *
     * @param coachUserId The user ID of the coach being assigned
     * @param assignedByUserId The user ID of the president performing the assignment
     * @param teamName The display name of the team
     */
    suspend operator fun invoke(
        coachUserId: String,
        assignedByUserId: String,
        teamName: String,
    )
}
