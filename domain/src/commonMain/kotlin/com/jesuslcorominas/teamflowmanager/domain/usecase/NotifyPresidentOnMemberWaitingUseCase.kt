package com.jesuslcorominas.teamflowmanager.domain.usecase

/**
 * Use case that notifies the president when a user joins the club but has no team assigned
 * and no pending team invitation. It creates a persistent Firestore notification entry
 * and sends an FCM push notification to the president.
 */
interface NotifyPresidentOnMemberWaitingUseCase {
    /**
     * Notifies the president of [clubId] that a user ([userName], [userEmail]) is waiting
     * for team assignment.
     *
     * @param clubId The Firestore document ID of the club
     * @param presidentUserId The Firebase user ID of the club president
     * @param userName The display name of the waiting user
     * @param userEmail The email of the waiting user
     */
    suspend operator fun invoke(
        clubId: String,
        presidentUserId: String,
        userName: String,
        userEmail: String,
    )
}
