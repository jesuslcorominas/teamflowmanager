package com.jesuslcorominas.teamflowmanager

import androidx.compose.runtime.mutableStateListOf
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole

/**
 * All navigable destinations in the iOS app.
 * Sealed hierarchy mirrors Android's Route + nav-args pattern without NavController.
 */
sealed class IosDestination {
    data object Splash : IosDestination()

    data object Login : IosDestination()

    data object Matches : IosDestination()

    data class Match(val matchId: String) : IosDestination()

    data object ArchivedMatches : IosDestination()

    data object Settings : IosDestination()

    data object ClubSelection : IosDestination()

    data object CreateClub : IosDestination()

    data object JoinClub : IosDestination()

    data object PendingTeamAssignment : IosDestination()

    data object TeamList : IosDestination()

    data class Team(val mode: String) : IosDestination()

    data object ClubMembers : IosDestination()

    data object Players : IosDestination()

    data class PlayerWizard(val playerId: String) : IosDestination()

    data class MatchWizard(val matchId: String) : IosDestination()

    data class AcceptTeamInvitation(val teamId: String?) : IosDestination()

    data object Analysis : IosDestination()

    data class PresidentTeamDetail(val teamId: String) : IosDestination()

    data class PresidentMatchDetail(val teamId: String, val matchId: String) : IosDestination()

    data object PresidentNotifications : IosDestination()

    data object ClubSettings : IosDestination()
}

/**
 * Stack-based navigation controller for iOS.
 *
 * Backed by [mutableStateListOf] so Compose reacts to push/pop automatically.
 */
class IosNavController {
    val backStack = mutableStateListOf<IosDestination>(IosDestination.Splash)

    val current: IosDestination get() = backStack.last()

    fun navigate(destination: IosDestination) {
        backStack.add(destination)
    }

    fun popBackStack() {
        if (backStack.size > 1) backStack.removeLast()
    }

    /** Clears the entire back stack and sets [destination] as the sole entry. */
    fun navigateClearing(destination: IosDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    /**
     * Lands on the first tab of [role] and drops the back stack of the role just left.
     *
     * Ignored while Settings is on screen. Settings only commits a role change when it is left,
     * so a change seen while it is still showing comes from the sign-out (or the account
     * deletion) clearing the stored role. Navigating then would take Settings off screen before
     * it reports the finished sign-out, and leave the user on an empty screen of the previous
     * account instead of the login (#540).
     *
     * @return whether it navigated.
     */
    fun navigateForRoleSwitch(role: ActiveViewRole): Boolean {
        if (current is IosDestination.Settings) return false
        val landing =
            when (role) {
                ActiveViewRole.President -> IosDestination.TeamList
                ActiveViewRole.Coach -> IosDestination.Matches
            }
        navigateClearing(landing)
        return true
    }

    /**
     * Navigates to Matches route, collapsing any deeper back-stack entries.
     * If Matches is already in the stack it pops to it; otherwise clears and navigates.
     */
    fun navigateToMatches() {
        val idx = backStack.indexOfLast { it is IosDestination.Matches }
        if (idx >= 0) {
            backStack.removeRange(idx + 1, backStack.size)
        } else {
            backStack.clear()
            backStack.add(IosDestination.Matches)
        }
    }
}
