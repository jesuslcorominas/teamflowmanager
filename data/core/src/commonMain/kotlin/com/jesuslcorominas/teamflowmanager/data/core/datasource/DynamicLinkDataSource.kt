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
     * @param teamId The document ID of the team
     * @param teamName The name of the team for display purposes
     * @return A shareable URL (either a short link or fallback custom scheme)
     *
     * The generated link should:
     * - Be clickable in messaging apps (WhatsApp, Email, SMS)
     * - Redirect to Play Store if app not installed
     * - Open the app with the team invitation deep link if installed
     */
    suspend fun generateTeamInvitationLink(
        teamId: String,
        teamName: String,
    ): String
}
