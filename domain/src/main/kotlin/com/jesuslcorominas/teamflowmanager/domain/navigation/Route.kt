package com.jesuslcorominas.teamflowmanager.domain.navigation

sealed class Route(
    protected val path: String,
    val showTopBar: Boolean = true,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
    val showFab: Boolean = false,
    val hasSearchBar: Boolean = false,
    val showSettingsButton: Boolean = false
) {

    companion object {
        val all by lazy {
            listOf(
                Splash,
                Login,
                Migration,
                ClubSelection,
                CreateClub,
                JoinClub,
                Players,
                Team,
                TeamList,
                ClubMembers,
                Matches,
                ArchivedMatches,
                CreateMatch,
                PlayerWizard,
                Match,
                Analysis,
                Settings,
            )
        }

        fun fromValue(value: String?): Route? {
            if (value == null) return null

            val base = value.substringBefore("?").substringBefore("/")

            return all.firstOrNull { it.path == base }
        }
    }

    fun createRoute(vararg params: Any?): String =
        path + if (params.isNotEmpty()) params.joinToString("") { "/$it" } else ""

    data class UiConfig(
        val showTopBar: Boolean,
        val showBottomBar: Boolean,
        val canGoBack: Boolean,
        val showFab: Boolean,
        val hasSearchBar: Boolean,
        val showSettingsButton: Boolean,
    )

    open fun uiConfig(arguments: Map<String, Any?>?): UiConfig =
        UiConfig(
            showTopBar = showTopBar,
            showBottomBar = showBottomBar,
            canGoBack = canGoBack,
            showFab = showFab,
            hasSearchBar = hasSearchBar,
            showSettingsButton = showSettingsButton,
        )

    data object Matches : Route(
        path = "matches",
        showBottomBar = true,
        showFab = true,
        hasSearchBar = true,
        showSettingsButton = true
    )

    data object Match : Route(path = "match", canGoBack = true) {
        const val ARG_MATCH_ID = "matchId"
        private const val PATH = "match"

        const val FULL_ROUTE = "$PATH/{$ARG_MATCH_ID}"
    }

    data object Settings : Route(path = "settings", canGoBack = true)

    data object Splash : Route(path = "splash", showTopBar = false)

    data object Login : Route(path = "login", showTopBar = false)

    data object Migration : Route(path = "migration", showTopBar = false)

    data object ClubSelection : Route(path = "club_selection", showTopBar = false)

    data object CreateClub : Route(path = "create_club", showTopBar = false)

    data object JoinClub : Route(path = "join_club", showTopBar = false)

    data object Team : Route(
        path = "team",
        showBottomBar = true
    ) {
        private const val PATH = "team"
        const val ARG_MODE = "mode"
        const val MODE_CREATE = "create"
        const val MODE_VIEW = "view"
        const val MODE_EDIT = "edit"

        const val FULL_ROUTE = "$PATH/{$ARG_MODE}"

        override fun uiConfig(arguments: Map<String, Any?>?): UiConfig {
            val mode = arguments?.get(ARG_MODE) as? String ?: MODE_VIEW
            return UiConfig(
                showTopBar = mode == MODE_EDIT || mode == MODE_VIEW,
                showBottomBar = mode == MODE_EDIT || mode == MODE_VIEW,
                canGoBack = mode == MODE_EDIT,
                showFab = mode == MODE_VIEW,
                hasSearchBar = false,
                showSettingsButton = mode == MODE_EDIT || mode == MODE_VIEW,
            )
        }
    }

    data object TeamList : Route(
        path = "team_list",
        showTopBar = true,
        showBottomBar = false,
        showFab = true,
        hasSearchBar = false,
        showSettingsButton = true
    )

    data object ClubMembers : Route(
        path = "club_members",
        showTopBar = true,
        showBottomBar = true,
        showSettingsButton = true
    )

    data object Players : Route(path = "players", showBottomBar = true, showSettingsButton = true)

    data object PlayerWizard : Route(path = "player_wizard", showTopBar = false) {
        const val ARG_PLAYER_ID = "playerId"
        private const val PATH = "player_wizard"
        const val FULL_ROUTE = "$PATH/{$ARG_PLAYER_ID}"
    }

    data object ArchivedMatches : Route(
        path = "archived_matches",
        showBottomBar = true,
        canGoBack = true,
        showSettingsButton = true
    )

    data object CreateMatch : Route(path = "create_match", showTopBar = false) {
        const val DEFAULT_MATCH_ID = 0L
        const val ARG_MATCH_ID = "matchId"
        private const val PATH = "create_match"

        const val FULL_ROUTE = "$PATH/{$ARG_MATCH_ID}"
    }

    data object Analysis : Route(path = "analysis", showBottomBar = true, showSettingsButton = true)
}
