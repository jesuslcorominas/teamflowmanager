package com.jesuslcorominas.teamflowmanager

import androidx.compose.runtime.mutableStateListOf

/**
 * All navigable destinations in the iOS app.
 * Sealed hierarchy mirrors Android's Route + nav-args pattern without NavController.
 */
sealed class IosDestination {
    data object Splash : IosDestination()
    data object Login : IosDestination()
    data object Matches : IosDestination()
    data class Match(val matchId: Long) : IosDestination()
    data object ArchivedMatches : IosDestination()
    data object Settings : IosDestination()
    data object ClubSelection : IosDestination()
    data object CreateClub : IosDestination()
    data object JoinClub : IosDestination()
    data object TeamList : IosDestination()
    data class Team(val mode: String) : IosDestination()
    data object ClubMembers : IosDestination()
    data object Players : IosDestination()
    data class PlayerWizard(val playerId: Long) : IosDestination()
    data class AcceptTeamInvitation(val teamId: String?) : IosDestination()
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
