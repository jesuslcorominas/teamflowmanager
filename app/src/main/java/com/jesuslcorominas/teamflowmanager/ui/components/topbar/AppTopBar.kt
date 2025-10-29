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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.main.search.SearchState
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    searchPlaceholder: String,
    backHandlerController: BackHandlerController,
    navController: NavHostController
) {
    val searchState = LocalSearchState.current

    if (uiConfig?.showTopBar == true) {
        Crossfade(
            targetState = searchState.isActive,
            animationSpec = tween(durationMillis = 300)
        ) { isSearchActive ->
            if (uiConfig.hasSearchBar && isSearchActive) {
                SearchTopBar(modifier = modifier, searchPlaceholder)
            } else {
                DefaultTopBar(
                    modifier = modifier,
                    uiConfig = uiConfig,
                    title = title,
                    backHandlerController = backHandlerController,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
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
                    IconButton(onClick = { searchState.isActive = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.close_search),
                        )
                    }
                },
                trailingIcon = {
                    if (searchState.query.isNotEmpty()) {
                        IconButton(onClick = { searchState.clear() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.3F), RoundedCornerShape(48.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier,
    uiConfig: Route.UiConfig,
    title: String?,
    backHandlerController: BackHandlerController,
    navController: NavHostController
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
                IconButton(
                    onClick = {
                        backHandlerController.onBackRequested?.invoke()
                            ?: navController.popBackStack()
                    }
                ) {
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
                        contentDescription = "Buscar"
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun SearchTopBarPreview() {
    TFMAppTheme {
        val dummySearchState = remember { SearchState() }

        CompositionLocalProvider(LocalSearchState provides dummySearchState) {
            SearchTopBar(
                placeholder = "Busca por rival o ubicación"
            )
        }
    }
}
