package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.form.ExpandableTitle
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.matches.card.ArchivedMatchesNavigationCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PausedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PendingMatchCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PlayedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.scrollToItem
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.androidx.compose.koinViewModel

private const val PENDING_MATCHES_HEADER = "pending_matches_header"
private const val PLAYED_MATCHES_HEADER = "played_matches_header"

@Composable
fun MatchListScreen(
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    onNavigateToArchivedMatches: () -> Unit,
    viewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MatchListUiState.Loading -> Loading()
            is MatchListUiState.Empty -> EmptyMatches(onNavigateToArchivedMatches = onNavigateToArchivedMatches)

            is MatchListUiState.Success -> MatchesList(
                state = state,
                onNavigateToArchivedMatches = onNavigateToArchivedMatches,
                onNavigateToEditMatch = onNavigateToEditMatch,
                onNavigateToMatch = onNavigateToMatch,
                viewModel = viewModel
            )
        }

        // Delete confirmation dialog
        if (deleteConfirmationState is MatchDeleteConfirmationState.Requested) {
            AppAlertDialog(
                title = stringResource(R.string.delete_match_title),
                message = stringResource(R.string.delete_match_message),
                confirmText = stringResource(R.string.delete),
                dismissText = stringResource(R.string.cancel),
                onConfirm = { viewModel.confirmDeleteMatch() },
                onDismiss = { viewModel.cancelDeleteMatch() },
            )
        }
    }
}


@Composable
private fun MatchesList(
    state: MatchListUiState.Success,
    onNavigateToArchivedMatches: () -> Unit,
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    viewModel: MatchListViewModel
) {
    val searchState = LocalSearchState.current

    val pendingMatches = state.matches.filter { it.status == MatchStatus.SCHEDULED }.sortedBy { it.dateTime }
    val activeMatch = state.matches.find { it.status == MatchStatus.IN_PROGRESS }
    val pausedMatch = state.matches.find { it.status == MatchStatus.PAUSED }
    val playedMatches = state.matches.filter { it.status == MatchStatus.FINISHED }.sortedByDescending { it.dateTime }

    val hasActiveMatch = activeMatch != null
    val hasPausedMatch = pausedMatch != null

    var expandedPendingMatches: Boolean by remember { mutableStateOf(true) }
    var expandedPlayedMatches: Boolean by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(searchState.query) {
        viewModel.onQueryChange(searchState.query)

        if (pendingMatches.isNotEmpty()) expandedPendingMatches = true
        if (playedMatches.isNotEmpty()) expandedPlayedMatches = true
    }

    DisposableEffect(Unit) {
        onDispose {
            searchState.isActive = false
            searchState.clear()
        }
    }

    if (state.matches.isEmpty()) {
        EmptyMatches(
            message = stringResource(R.string.no_results),
            onNavigateToArchivedMatches = onNavigateToArchivedMatches
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = TFMSpacing.spacing04,
                start = TFMSpacing.spacing04,
                end = TFMSpacing.spacing04
            ),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        item { ArchivedMatchesNavigationCard(onClick = onNavigateToArchivedMatches) }

        // Active match section (if exists, show at top)
        if (hasActiveMatch) {
            item {
                Text(
                    text = stringResource(R.string.current_match_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                )
            }
            item {
                PausedMatchCard(
                    match = activeMatch,
                    onResume = {
                        onNavigateToMatch(activeMatch)
                    },
                    onNavigateToDetail = {
                        onNavigateToMatch(activeMatch)
                    },
                )
            }
        }

        // Paused match section (if exists, show at top)
        if (hasPausedMatch) {
            item {
                Text(
                    text = stringResource(R.string.paused_match_half_time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                )
            }
            item {
                PausedMatchCard(
                    match = pausedMatch,
                    onResume = {
                        viewModel.resumeMatch(pausedMatch.id)
                        onNavigateToMatch(pausedMatch)
                    },
                    onNavigateToDetail = {
                        onNavigateToMatch(pausedMatch)
                    },
                )
            }
        }

        if (pendingMatches.isNotEmpty()) {
            pendingMatchesSection(
                pendingMatches = pendingMatches,
                expandedPendingMatches = expandedPendingMatches,
                onNavigateToEditMatch = onNavigateToEditMatch,
                onNavigateToMatch = onNavigateToMatch,
                hasMatchStarted = hasActiveMatch || hasPausedMatch,
                onDeleteMatch = { viewModel.requestDeleteMatch(it) },
                onExpandToggle = {
                    expandedPendingMatches = !expandedPendingMatches

                    if (expandedPendingMatches) {
                        scrollToItem(PENDING_MATCHES_HEADER, listState, coroutineScope)
                    }
                },
            )
        }

        if (playedMatches.isNotEmpty()) {
            playedMatchesSection(
                playedMatches = playedMatches,
                expandedPlayedMatches = expandedPlayedMatches,
                onNavigateToMatch = onNavigateToMatch,
                onArchiveMatch = { viewModel.archiveMatch(it) },
                onExpandToggle = {
                    expandedPlayedMatches = !expandedPlayedMatches

                    if (expandedPlayedMatches) {
                        scrollToItem(PLAYED_MATCHES_HEADER, listState, coroutineScope)
                    }
                },
            )
        }

        item {
            // empty spacer to allow scrolling past last item
            Box(Modifier.height(TFMSpacing.spacing11))
        }
    }
}

private fun LazyListScope.pendingMatchesSection(
    pendingMatches: List<Match>,
    expandedPendingMatches: Boolean,
    hasMatchStarted: Boolean,
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatch: (Match) -> Unit,
    onDeleteMatch: (Match) -> Unit,
    onExpandToggle: () -> Unit,
) {
    item(key = PENDING_MATCHES_HEADER) {
        ExpandableTitle(
            title = stringResource(R.string.pending_matches),
            expanded = expandedPendingMatches,
            onClick = onExpandToggle
        )
    }

    if (expandedPendingMatches) {
        items(pendingMatches) { match ->
            PendingMatchCard(
                match = match,
                hasMatchStarted = hasMatchStarted,
                onEdit = { onNavigateToEditMatch(match.id) },
                onDelete = { onDeleteMatch(match) },
                onStart = { onNavigateToMatch(match) },
            )
        }
    }
}

private fun LazyListScope.playedMatchesSection(
    playedMatches: List<Match>,
    expandedPlayedMatches: Boolean,
    onNavigateToMatch: (Match) -> Unit,
    onArchiveMatch: (Long) -> Unit,
    onExpandToggle: () -> Unit,
) {
    item(key = PLAYED_MATCHES_HEADER) {
        ExpandableTitle(
            title = stringResource(R.string.played_matches),
            expanded = expandedPlayedMatches,
            onClick = onExpandToggle
        )
    }

    if (expandedPlayedMatches) {
        items(playedMatches) { match ->
            PlayedMatchCard(
                match = match,
                onNavigateToDetail = {
                    onNavigateToMatch(match)
                },
                onAction = { onArchiveMatch(match.id) },
            )
        }
    }
}

// region Empty
@Composable
private fun EmptyMatches(
    message: String = stringResource(R.string.no_matches_message),
    onNavigateToArchivedMatches: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TFMSpacing.spacing04),
    ) {
        ArchivedMatchesNavigationCard(onClick = onNavigateToArchivedMatches)

        EmptyContent(message)
    }
}

@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true
)
@Composable
fun EmptyMatchesPreview() {
    TFMAppTheme {
        EmptyMatches(onNavigateToArchivedMatches = {})
    }
}


// endregion
