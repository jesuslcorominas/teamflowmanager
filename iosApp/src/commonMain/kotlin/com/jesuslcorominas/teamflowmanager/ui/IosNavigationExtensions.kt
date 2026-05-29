package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.IosDestination
import com.jesuslcorominas.teamflowmanager.IosNavController
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PresidentMatchDetailScaffold(
    title: String?,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (title != null) Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
        ) {
            content()
        }
    }
}

internal fun IosDestination.toRouteString(): String =
    when (this) {
        is IosDestination.Matches -> Route.Matches.createRoute()
        is IosDestination.Match -> Route.Match.createRoute(matchId)
        is IosDestination.ArchivedMatches -> Route.ArchivedMatches.createRoute()
        is IosDestination.Settings -> Route.Settings.createRoute()
        is IosDestination.Team -> Route.Team.createRoute(mode)
        is IosDestination.TeamList -> Route.TeamList.createRoute()
        is IosDestination.ClubMembers -> Route.ClubMembers.createRoute()
        is IosDestination.Players -> Route.Players.createRoute()
        is IosDestination.Analysis -> Route.Analysis.createRoute()
        is IosDestination.PresidentNotifications -> Route.PresidentNotifications.createRoute()
        is IosDestination.ClubSettings -> Route.ClubSettings.createRoute()
        is IosDestination.PresidentTeamDetail -> Route.PresidentTeamDetail.createRoute()
        is IosDestination.PresidentMatchDetail -> Route.PresidentMatchDetail.createRoute()
        else -> ""
    }

internal fun IosNavController.navigateToBottomNav(route: String) {
    val dest: IosDestination =
        when (Route.fromValue(route)) {
            Route.Matches -> IosDestination.Matches
            Route.Team -> IosDestination.Team(Route.Team.MODE_VIEW)
            Route.TeamList -> IosDestination.TeamList
            Route.ClubMembers -> IosDestination.ClubMembers
            Route.Players -> IosDestination.Players
            Route.Analysis -> IosDestination.Analysis
            Route.PresidentNotifications -> IosDestination.PresidentNotifications
            Route.ClubSettings -> IosDestination.ClubSettings
            else -> return
        }
    navigateClearing(dest)
}
