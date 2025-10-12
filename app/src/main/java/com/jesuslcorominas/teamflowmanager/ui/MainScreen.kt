package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.ui.match.MatchDetailScreen
import org.koin.androidx.compose.get
import java.util.Date

@Composable
fun MainScreen() {
    var showMatchDetail by remember { mutableStateOf(false) }
    val matchRepository: MatchRepository = get()
    val demoMatchId = "demo-match-1"

    // Initialize a demo match
    LaunchedEffect(Unit) {
        val demoMatch = Match(
            id = demoMatchId,
            teamId = "team1",
            opponent = "Opponent Team",
            startTime = Date(),
            status = MatchStatus.IN_PROGRESS,
            firstHalfDuration = 0,
            secondHalfDuration = 0
        )
        matchRepository.updateMatch(demoMatch)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (showMatchDetail) {
            MatchDetailScreen(matchId = demoMatchId)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Button(
                    onClick = { showMatchDetail = true },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("View Match Detail (Demo)")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen()
    }
}
