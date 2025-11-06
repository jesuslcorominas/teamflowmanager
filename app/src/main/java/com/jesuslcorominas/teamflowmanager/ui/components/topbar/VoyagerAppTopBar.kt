package com.jesuslcorominas.teamflowmanager.ui.components.topbar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyagerAppTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    searchPlaceholder: String,
    onBackClick: () -> Unit
) {
    val searchState = LocalSearchState.current

    if (uiConfig?.showTopBar == true) {
        Crossfade(
            targetState = searchState.isActive,
            animationSpec = tween(durationMillis = 300),
            label = "topbar_crossfade"
        ) { isSearchActive ->
            if (uiConfig.hasSearchBar && isSearchActive) {
                SearchTopBar(modifier = modifier, searchPlaceholder)
            } else {
                VoyagerDefaultTopBar(
                    modifier = modifier,
                    uiConfig = uiConfig,
                    title = title,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoyagerDefaultTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig,
    title: String?,
    onBackClick: () -> Unit
) {
    val searchState = LocalSearchState.current

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title ?: "",
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (uiConfig.canGoBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.close),
                    )
                }
            }
        },
        actions = {
            if (uiConfig.hasSearchBar && !searchState.isActive) {
                IconButton(
                    onClick = { searchState.isActive = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
        },
    )
}
