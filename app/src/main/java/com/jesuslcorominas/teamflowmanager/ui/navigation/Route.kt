package com.jesuslcorominas.teamflowmanager.ui.navigation


sealed class Route(
    val path: String,
    val showTopBar: Boolean = false,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
    val showFab: Boolean = false,
) {

    companion object {
        val all by lazy {
            listOf(
                Splash,
                CreateTeam,
                Players,
                TeamDetail,
                Matches,
                ArchivedMatches,
                CreateMatch,
                CurrentMatch,
                MatchDetail,
                MatchSummary,
            )
        }

        fun fromValue(value: String?): Route? {
            val base = value?.substringBefore("/")
            return all.firstOrNull { it.path == base }
        }
    }

    fun createRoute(): String = path

    open fun uiConfig(arguments: Map<String, Any?>?): UiConfig =
        UiConfig(
            showTopBar = showTopBar,
            showBottomBar = showBottomBar,
            canGoBack = canGoBack,
            showFab = showFab,
        )

    data class UiConfig(
        val showTopBar: Boolean,
        val showBottomBar: Boolean,
        val canGoBack: Boolean,
        val showFab: Boolean,
    )

    object Splash : Route(
        path = "splash",
        showTopBar = false,
        showBottomBar = false,
        canGoBack = false,
    )

    object CreateTeam : Route(
        path = "create_team",
        showTopBar = false,
        showBottomBar = false,
        canGoBack = false,
    )

    object Players : Route(
        path = "players",
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object TeamDetail : Route(
        path = "team_detail",
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object Matches : Route(
        path = "matches",
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
        showFab = true,
    )

    object ArchivedMatches : Route(
        path = "archived_matches",
        showTopBar = false,
        showBottomBar = false,
        canGoBack = true,
    )
    
    object CreateMatch : Route(
        path = "create_match",
        showTopBar = true,
        showBottomBar = false,
        canGoBack = false,
    )

    object CurrentMatch : Route(
        path = "current_match",
        showTopBar = true,
        showBottomBar = false,
        canGoBack = true,
    )

    object MatchDetail : Route(
        path = "match_detail",
        showTopBar = true,
        showBottomBar = false,
        canGoBack = true,
    ) {
        fun createRoute(matchId: Long?): String {
            return if (matchId != null) "$path/$matchId" else path
        }
    }

    object MatchSummary : Route(
        path = "match_summary",
        showTopBar = true,
        showBottomBar = false,
        canGoBack = true,
    ) {
        fun createRoute(matchId: Long): String {
            return "$path/$matchId"
        }
    }
}
