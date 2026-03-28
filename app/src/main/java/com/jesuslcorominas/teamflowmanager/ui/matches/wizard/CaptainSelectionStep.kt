package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun CaptainSelectionStep(
    players: List<Player>,
    selectedCaptainId: Long?,
    onCaptainChanged: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentCaptainId by remember(selectedCaptainId) { mutableLongStateOf(selectedCaptainId ?: 0L) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        Text(
            text = stringResource(R.string.captain_selection_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        PlayerList(
            players = players.sortedBy { it.number },
            modifier = Modifier.weight(1F),
            showPositions = false,
            showGoalKeeperBadge = true,
            paddingValues = PaddingValues(TFMSpacing.spacing02),
            selectedPlayerIds = setOf(currentCaptainId).map { it }.toSet(),
            onSingleSelectionChange = { player ->
                currentCaptainId = player.id
            },
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Button(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.previous))
            }

            Button(
                onClick = {
                    onCaptainChanged(currentCaptainId)
                    onNext()
                },
                enabled = currentCaptainId != 0L,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun CaptainSelectionStepPreview() {
    TFMAppTheme {
        CaptainSelectionStep(
            players =
                (1..5).map {
                    Player(
                        id = it.toLong(),
                        firstName = "John",
                        lastName = "Doe",
                        number = 10,
                        positions = listOf(Position.Forward, Position.Midfielder),
                        teamId = 1,
                        isCaptain = false,
                    )
                },
            selectedCaptainId = null,
            onCaptainChanged = {},
            onNext = {},
            onPrevious = {},
        )
    }
}
