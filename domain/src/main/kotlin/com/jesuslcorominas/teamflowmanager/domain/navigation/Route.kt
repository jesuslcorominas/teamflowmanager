package com.jesuslcorominas.teamflowmanager.domain.navigation

sealed class Route(
    protected val path: String,
    val showTopBar: Boolean = true,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
    val showFab: Boolean = false,
    val hasSearchBar: Boolean = false
) {

    companion object {
        val all by lazy {
            listOf(
                Splash,
                Players,
                Team,
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
            val base = value?.substringBefore("/")
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
    )

    open fun uiConfig(arguments: Map<String, Any?>?): UiConfig =
        UiConfig(
            showTopBar = showTopBar,
            showBottomBar = showBottomBar,
            canGoBack = canGoBack,
            showFab = showFab,
            hasSearchBar = hasSearchBar,
        )

    data object Splash : Route(path = "splash", showTopBar = false)

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
            )
        }
    }

    data object Players : Route(path = "players", showBottomBar = true)

    data object PlayerWizard : Route(path = "player_wizard", showTopBar = false) {
        const val ARG_PLAYER_ID = "playerId"
        private const val PATH = "player_wizard"
        const val FULL_ROUTE = "$PATH/{$ARG_PLAYER_ID}"
    }

    data object Matches : Route(
        path = "matches",
        showBottomBar = true,
        showFab = true,
        hasSearchBar = true
    )

    data object ArchivedMatches : Route(
        path = "archived_matches",
        showBottomBar = true,
        canGoBack = true,
    )

    data object CreateMatch : Route(path = "create_match", showTopBar = false) {
        const val DEFAULT_MATCH_ID = 0L
        const val ARG_MATCH_ID = "matchId"
        private const val PATH = "create_match"

        const val FULL_ROUTE = "$PATH/{$ARG_MATCH_ID}"
    }

    data object Match : Route(path = "match", canGoBack = true) {
        const val ARG_MATCH_ID = "matchId"
        const val ARG_TEAM = "team"
        const val ARG_OPPONENT = "opponent"
        private const val PATH = "match"

        const val FULL_ROUTE = "$PATH/{$ARG_MATCH_ID}/{$ARG_TEAM}/{$ARG_OPPONENT}"
    }

    data object Analysis : Route(path = "analysis", showBottomBar = true)

    data object Settings : Route(path = "settings", canGoBack = true)
}
