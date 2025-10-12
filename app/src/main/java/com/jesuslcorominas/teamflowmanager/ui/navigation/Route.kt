package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector
import com.jesuslcorominas.teamflowmanager.R

sealed class Route(
    val path: String,
    val icon: ImageVector? = null,
    @StringRes val label: Int? = null,
    val showTopBar: Boolean = false,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
) {

    companion object {
        val all =
            listOf(
                Splash,
                CreateTeam,
                Players,
                TeamDetail,
                Matches,
                CurrentMatch,
                MatchDetail,
            )

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
        )

    data class UiConfig(
        val showTopBar: Boolean,
        val showBottomBar: Boolean,
        val canGoBack: Boolean,
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
        icon = Icons.Default.Group,
        label = R.string.nav_players,
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object TeamDetail : Route(
        path = "team_detail",
        icon = Icons.Default.Groups,
        label = R.string.nav_team,
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object Matches : Route(
        path = "matches",
        icon = Icons.Default.SportsSoccer,
        label = R.string.nav_matches,
        showTopBar = true,
        showBottomBar = true,
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
}
