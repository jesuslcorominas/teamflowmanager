package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class SearchState {
    var query by mutableStateOf("")
    var isActive by mutableStateOf(false)

    fun clear() {
        query = ""
    }
}

@Composable
fun rememberSearchState(): SearchState {
    return remember { SearchState() }
}

val LocalSearchState = staticCompositionLocalOf<SearchState> {
    error("No SearchState provided")
}
