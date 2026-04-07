package com.jesuslcorominas.teamflowmanager.usecase.repository

/**
 * Repository for sending push notifications related to coach assignment.
 */
interface CoachAssignmentNotificationRepository {
    /**
     * Sends a push notification to all FCM tokens registered for [coachUserId]
     * informing them that they have been assigned to [teamName].
     *
     * @param coachUserId The Firebase user ID of the coach to notify
     * @param teamName The name of the team to which the coach was assigned
     */
    suspend fun notifyCoachAssigned(
        coachUserId: String,
        teamName: String,
    )
}
