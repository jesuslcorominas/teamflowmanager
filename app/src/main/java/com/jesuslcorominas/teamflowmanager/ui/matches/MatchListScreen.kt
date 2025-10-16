package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchDeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchListScreen(
    onNavigateToEditMatch: (Long) -> Unit,
    onNavigateToMatchSummary: (Long) -> Unit,
    onNavigateToCurrentMatch: () -> Unit,
    onNavigateToArchivedMatches: () -> Unit,
    viewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MatchListUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is MatchListUiState.Empty -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(TFMSpacing.spacing04),
                    verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                ) {
                    // Filter button
                    FilterButton(
                        isFilterModeEnabled = filterState.isFilterModeEnabled,
                        onClick = { viewModel.toggleFilterMode() },
                    )

                    // Search bar (only show when filter mode is enabled)
                    if (filterState.isFilterModeEnabled) {
                        SearchBar(
                            searchText = filterState.searchText,
                            onSearchTextChanged = { viewModel.updateSearchText(it) },
                            onClearSearch = { viewModel.clearFilters() },
                            keyboardController = keyboardController,
                        )
                    }

                    ArchivedMatchesNavigationCard(
                        onClick = onNavigateToArchivedMatches,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.no_matches_message),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            is MatchListUiState.Success -> {
                val currentMatchId = state.currentMatchId
                
                // When filtering, all matches are in a flat list
                val isFiltering = filterState.isActive
                
                if (isFiltering) {
                    // Filtered view - show all matches in a single list
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(TFMSpacing.spacing04),
                        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                    ) {
                        // Filter controls
                        item {
                            FilterButton(
                                isFilterModeEnabled = filterState.isFilterModeEnabled,
                                onClick = { viewModel.toggleFilterMode() },
                            )
                        }
                        
                        item {
                            SearchBar(
                                searchText = filterState.searchText,
                                onSearchTextChanged = { viewModel.updateSearchText(it) },
                                onClearSearch = { viewModel.clearFilters() },
                                keyboardController = keyboardController,
                            )
                        }
                        
                        // Filtered matches
                        items(state.matches) { match ->
                            FilteredMatchCard(
                                match = match,
                                onNavigateToDetail = {
                                    if (match.archived) {
                                        onNavigateToMatchSummary(match.id)
                                    } else if (match.elapsedTimeMillis > 0L) {
                                        onNavigateToMatchSummary(match.id)
                                    } else {
                                        onNavigateToEditMatch(match.id)
                                    }
                                },
                            )
                        }
                    }
                } else {
                    // Normal view - categorized by status
                    val pendingMatches = state.matches.filter { it.elapsedTimeMillis == 0L && !it.isRunning }
                    val pausedMatch = if (currentMatchId != null) {
                        state.matches.find { it.id == currentMatchId && !it.isRunning }
                    } else null
                    val playedMatches = state.matches.filter {
                        it.elapsedTimeMillis > 0L && !it.isRunning && it.id != currentMatchId
                    }
                    val hasActiveMatch = state.matches.any { it.isRunning }
                    val hasPausedMatch = pausedMatch != null

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(TFMSpacing.spacing04),
                        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                    ) {
                        // Filter button
                        item {
                            FilterButton(
                                isFilterModeEnabled = filterState.isFilterModeEnabled,
                                onClick = { viewModel.toggleFilterMode() },
                            )
                        }

                        // Archived matches navigation item (WhatsApp-style)
                        item {
                            ArchivedMatchesNavigationCard(
                                onClick = onNavigateToArchivedMatches,
                            )
                        }

                        // Paused match section (if exists, show at top)
                        if (hasPausedMatch) {
                            item {
                                Text(
                                    text = stringResource(R.string.paused_match),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                                )
                            }
                            item {
                                PausedMatchCard(
                                    match = pausedMatch,
                                    onResume = {
                                        viewModel.resumeMatch()
                                        onNavigateToCurrentMatch()
                                    },
                                    onNavigateToDetail = { onNavigateToCurrentMatch() },
                                )
                            }
                        }

                        // Pending matches section
                        if (pendingMatches.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.pending_matches),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                                )
                            }
                            items(pendingMatches) { match ->
                                PendingMatchCard(
                                    match = match,
                                    hasActiveMatch = hasActiveMatch || hasPausedMatch,
                                    onEdit = { onNavigateToEditMatch(match.id) },
                                    onDelete = { viewModel.requestDeleteMatch(match) },
                                    onStart = {
                                        if (!hasActiveMatch && !hasPausedMatch) {
                                            viewModel.startMatch(match.id)
                                            onNavigateToCurrentMatch()
                                        }
                                    },
                                )
                            }
                        }

                        // Played matches section
                        if (playedMatches.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))
                                Text(
                                    text = stringResource(R.string.played_matches),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = TFMSpacing.spacing02),
                                )
                            }
                            items(playedMatches) { match ->
                                PlayedMatchCard(
                                    match = match,
                                    onNavigateToDetail = { onNavigateToMatchSummary(match.id) },
                                    onArchive = { viewModel.archiveMatch(match.id) },
                                )
                            }
                        }
                    }
                }
            }
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
fun PendingMatchCard(
    match: Match,
    hasActiveMatch: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = match.opponent ?: stringResource(R.string.opponent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.location ?: stringResource(R.string.location),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val date = match.date?.let { DateFormatter.formatDate(it) } ?: ""
                    val time = match.time?.let { DateFormatter.formatTimeOfDay(it) } ?: ""

                    val dateTime = listOf(date, time).filter { it.isNotEmpty() }.joinToString(" ")

                    if (dateTime.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                        Text(
                            text = dateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasActiveMatch,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.padding(start = TFMSpacing.spacing01))
                Text(text = stringResource(R.string.start_match))
            }
        }
    }
}

@Composable
fun PausedMatchCard(
    match: Match,
    onResume: () -> Unit,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetail() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = match.opponent ?: stringResource(R.string.opponent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.location ?: stringResource(R.string.location),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    match.date?.let {
                        Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                        Text(
                            text = DateFormatter.formatDate(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.padding(start = TFMSpacing.spacing01))
                    Text(text = stringResource(R.string.resume_match_button))
                }

                Button(
                    onClick = onNavigateToDetail,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.view_match))
                }
            }
        }
    }
}

@Composable
fun PlayedMatchCard(
    modifier: Modifier = Modifier,
    match: Match,
    onNavigateToDetail: () -> Unit = {},
    onArchive: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetail() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = match.opponent ?: stringResource(R.string.opponent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                Text(
                    text = match.location ?: stringResource(R.string.location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (match.date != null) {
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.date?.let { DateFormatter.formatDateTime(it) } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // TODO: Show actual score when score tracking is implemented
                Text(
                    text = stringResource(R.string.match_score, 0, 0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = onArchive) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = stringResource(R.string.archive_match),
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedMatchesNavigationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.padding(start = TFMSpacing.spacing03))
                Text(
                    text = stringResource(R.string.archived_matches),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun FilterButton(
    isFilterModeEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.filter_matches),
                tint = if (isFilterModeEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_by_opponent_or_location)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                Row {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_filters),
                        )
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        )
    )
}

@Composable
fun FilteredMatchCard(
    match: Match,
    onNavigateToDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetail() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02)
                ) {
                    Text(
                        text = match.opponent ?: stringResource(R.string.opponent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    
                    // Show archived badge
                    if (match.archived) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = stringResource(R.string.archived_indicator),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = TFMSpacing.spacing01)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                Text(
                    text = match.location ?: stringResource(R.string.location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (match.date != null) {
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.date?.let { DateFormatter.formatDateTime(it) } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = if (match.archived) FontStyle.Italic else FontStyle.Normal,
                    )
                }
            }
        }
    }
}

