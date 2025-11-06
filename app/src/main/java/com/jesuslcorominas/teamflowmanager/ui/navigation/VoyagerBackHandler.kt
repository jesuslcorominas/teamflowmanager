package com.jesuslcorominas.teamflowmanager.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.screens.MainTabScreen
import com.jesuslcorominas.teamflowmanager.ui.screens.MatchesTab
import com.jesuslcorominas.teamflowmanager.ui.screens.TeamTab

@Composable
fun VoyagerBackHandler() {
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as? Activity
    val searchState = LocalSearchState.current

    // Check if we're in the MainTabScreen
    val isInMainTab = navigator.lastItem is MainTabScreen

    if (isInMainTab) {
        // Handle back in tab navigation
        TabBackHandler(activity, searchState)
    } else {
        // Handle back in regular navigation
        BackHandler {
            if (!navigator.pop()) {
                activity?.finish()
            }
        }
    }
}

@Composable
private fun TabBackHandler(activity: Activity?, searchState: com.jesuslcorominas.teamflowmanager.ui.main.search.SearchState) {
    val tabNavigator = LocalTabNavigator.current

    BackHandler {
        when (val currentTab = tabNavigator.current) {
            is MatchesTab -> {
                if (searchState.isActive) {
                    searchState.clear()
                    searchState.isActive = false
                } else {
                    activity?.finish()
                }
            }
            is TeamTab -> {
                // Navigate back to matches
                tabNavigator.current = MatchesTab
            }
            else -> {
                // Navigate back to matches from any other tab
                tabNavigator.current = MatchesTab
            }
        }
    }
}
