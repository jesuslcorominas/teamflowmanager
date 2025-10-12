package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class Route(
    protected val path: String,
    @DrawableRes val icon: Int? = null,
    @StringRes val label: Int? = null,
    val showTopBar: Boolean = false,
    val showBottomBar: Boolean = false,
    val canGoBack: Boolean = false,
) {

    companion object {
        val all =
            listOf(
                Players,
                CurrentMatch,
                Matches,
                MatchDetail,
                Team,
            )

        fun fromValue(value: String?): Route? {
            val base = value?.substringBefore("/")
            return all.firstOrNull { it.path == base }
        }
    }

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

    object Players : Route(
        path = "players",
        icon = null,
        label = null,
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object CurrentMatch : Route(
        path = "current_match",
        icon = null,
        label = null,
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object Matches : Route(
        path = "matches",
        icon = null,
        label = null,
        showTopBar = true,
        showBottomBar = true,
        canGoBack = false,
    )

    object MatchDetail : Route(
        path = "matchDetail",
        icon = null,
        label = null,
        showTopBar = true,
        showBottomBar = false,
        canGoBack = true,
    ) {
        override fun uiConfig(arguments: Map<String, Any?>?): UiConfig {
            val canGoBack =
                (arguments?.get("canGoBack") as? Boolean) ?: super.uiConfig(arguments).canGoBack
            return super.uiConfig(arguments).copy(canGoBack = canGoBack)
        }
    }

    object Team : Route(
        path = "team",
        icon = null,
        label = null,
        showTopBar = true,
        showBottomBar = false,
        canGoBack = false,
    )
}
