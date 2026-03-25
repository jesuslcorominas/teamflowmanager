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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.main.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.clear
import teamflowmanager.shared_ui.generated.resources.close
import teamflowmanager.shared_ui.generated.resources.close_search
import teamflowmanager.shared_ui.generated.resources.search
import teamflowmanager.shared_ui.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    searchPlaceholder: String = "",
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    val searchState = LocalSearchState.current

    if (uiConfig?.showTopBar == true) {
        Crossfade(
            targetState = searchState.isActive,
            animationSpec = tween(durationMillis = 300),
        ) { isSearchActive ->
            if (uiConfig.hasSearchBar && isSearchActive) {
                SearchTopBar(modifier = modifier, placeholder = searchPlaceholder)
            } else {
                DefaultTopBar(
                    modifier = modifier,
                    uiConfig = uiConfig,
                    title = title,
                    onBack = onBack,
                    onSettings = onSettings,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    modifier: Modifier = Modifier,
    placeholder: String,
) {
    val searchState = LocalSearchState.current

    TopAppBar(
        modifier = modifier,
        title = {
            TextField(
                value = searchState.query,
                onValueChange = { searchState.query = it },
                placeholder = { Text(placeholder) },
                singleLine = true,
                leadingIcon = {
                    IconButton(
                        onClick = {
                            searchState.clear()
                            searchState.isActive = false
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.close_search),
                        )
                    }
                },
                trailingIcon = {
                    if (searchState.query.isNotEmpty()) {
                        IconButton(onClick = { searchState.clear() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.clear),
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(48.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                        .background(Color.LightGray.copy(alpha = 0.3F), RoundedCornerShape(48.dp)),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig,
    title: String?,
    onBack: () -> Unit,
    onSettings: () -> Unit,
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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.close),
                    )
                }
            }
        },
        actions = {
            if (uiConfig.hasSearchBar && !searchState.isActive) {
                IconButton(onClick = { searchState.isActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(Res.string.search),
                    )
                }
            }
            if (uiConfig.showSettingsButton) {
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(Res.string.settings),
                    )
                }
            }
        },
    )
}
