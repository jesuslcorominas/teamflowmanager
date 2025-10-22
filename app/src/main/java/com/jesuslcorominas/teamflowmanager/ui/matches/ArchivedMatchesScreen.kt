package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.matches.card.ArchivedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.ArchivedMatchesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedMatchesScreen(
    onNavigateToMatchSummary: (Long, String, String) -> Unit,
    viewModel: ArchivedMatchesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ArchivedMatchesUiState.Loading -> Loading()

            is ArchivedMatchesUiState.Empty -> EmptyContent(stringResource(R.string.no_archived_matches))

            is ArchivedMatchesUiState.Success -> {
                ArchivedMatches(
                    matches = state.matches,
                    onNavigateToMatchSummary = onNavigateToMatchSummary,
                    unarchiveMatch = { viewModel.unarchiveMatch(it) }
                )
            }
        }
    }
}

@Composable
private fun ArchivedMatches(
    matches: List<Match>,
    onNavigateToMatchSummary: (Long, String, String) -> Unit,
    unarchiveMatch: (Long) -> Unit
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        items(matches) { match ->
            ArchivedMatchCard(
                match = match,
                onNavigateToDetail = { onNavigateToMatchSummary(match.id, match.teamName, match.opponent) },
                onUnarchive = { unarchiveMatch(match.id) },
            )
        }
    }
}


@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ArchivedMatchesPreview() {
    TFMAppTheme {
        (1..3).map {
            Match(
                id = it.toLong(),
                opponent = "Opponent $it",
                location = "Location $it",
                dateTime = System.currentTimeMillis(),
                teamId = 1,
                teamName = "Loyola D",
                numberOfPeriods = 2,
                squadCallUpIds = listOf(1, 2, 3, 4, 5),
                captainId = 1,
                startingLineupIds = listOf(1, 2, 3, 4, 5),
                elapsedTimeMillis = System.currentTimeMillis(),
                lastStartTimeMillis = System.currentTimeMillis(),
                status = MatchStatus.FINISHED,
                archived = true,
                currentPeriod = 2,
                pauseCount = 1,
            )
        }.let { matches ->
            ArchivedMatches(
                matches = matches,
                onNavigateToMatchSummary = { _, _, _ -> },
                unarchiveMatch = {}
            )
        }
    }
}
