package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * Data source for generating dynamic links for team invitations.
 * 
 * This abstraction allows the use case layer to remain Android-agnostic
 * while the implementation in the data-remote module uses Firebase Dynamic Links.
 */
interface DynamicLinkDataSource {
    /**
     * Generates a dynamic link for team invitation.
     * 
     * @param teamFirestoreId The Firestore document ID of the team
     * @param teamName The name of the team for display purposes
     * @return A shareable URL (either Firebase Dynamic Link or fallback custom scheme)
     * 
     * The generated link should:
     * - Be clickable in messaging apps (WhatsApp, Email, SMS)
     * - Redirect to Play Store if app not installed
     * - Open the app with the team invitation deep link if installed
     */
    suspend fun generateTeamInvitationLink(teamFirestoreId: String, teamName: String): String
}
