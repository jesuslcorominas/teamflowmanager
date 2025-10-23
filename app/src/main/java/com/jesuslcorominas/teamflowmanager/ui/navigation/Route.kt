package com.jesuslcorominas.teamflowmanager.ui.navigation


sealed class Route(
    protected val path: String,
    val showTopBar: Boolean = true,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
    val showFab: Boolean = false
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
                Match,
                MatchDetail,
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
    )

    open fun uiConfig(arguments: Map<String, Any?>?): UiConfig =
        UiConfig(
            showTopBar = showTopBar,
            showBottomBar = showBottomBar,
            canGoBack = canGoBack,
            showFab = showFab,
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
                showFab = mode == MODE_VIEW
            )
        }
    }

    data object Players : Route(path = "players", showBottomBar = true)

    data object Matches : Route(
        path = "matches",
        showBottomBar = true,
        showFab = true,
    )

    data object ArchivedMatches : Route(
        path = "archived_matches",
        showBottomBar = true,
        canGoBack = true,
    )

    data object CreateMatch : Route(path = "create_match", showTopBar = false)

    data object Match : Route(path = "match", canGoBack = true) {
        const val ARG_MATCH_ID = "matchId"
        const val ARG_TEAM = "team"
        const val ARG_OPPONENT = "opponent"
        private const val PATH = "match"

        const val FULL_ROUTE = "$PATH/{$ARG_MATCH_ID}/{$ARG_TEAM}/{$ARG_OPPONENT}"
    }

    // TODO remove this screen
    data object MatchDetail : Route(path = "match_detail", canGoBack = true) {
        const val ARG_MATCH_ID = "matchId"
    }
}
