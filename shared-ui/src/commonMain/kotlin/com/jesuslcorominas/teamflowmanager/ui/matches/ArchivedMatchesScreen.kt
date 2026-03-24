package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PlayedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.no_archived_matches

@Composable
fun ArchivedMatchesScreen(
    onNavigateToMatchSummary: (Match) -> Unit,
    viewModel: ArchivedMatchesViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.ARCHIVED_MATCHES, screenClass = "ArchivedMatchesScreen")

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ArchivedMatchesUiState.Loading -> Loading()
            is ArchivedMatchesUiState.Empty -> EmptyContent(stringResource(Res.string.no_archived_matches))
            is ArchivedMatchesUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = LocalContentBottomPadding.current,
                        top = TFMSpacing.spacing04,
                        start = TFMSpacing.spacing04,
                        end = TFMSpacing.spacing04,
                    ),
                    verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                ) {
                    items(state.matches) { match ->
                        PlayedMatchCard(
                            match = match,
                            onNavigateToDetail = { onNavigateToMatchSummary(match) },
                            onAction = { viewModel.unarchiveMatch(match.id) },
                        )
                    }
                }
            }
        }
    }
}
