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
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.captain_selection_subtitle
import teamflowmanager.shared_ui.generated.resources.captain_selection_title
import teamflowmanager.shared_ui.generated.resources.next
import teamflowmanager.shared_ui.generated.resources.previous

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
            text = stringResource(Res.string.captain_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.captain_selection_subtitle),
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
            selectedPlayerIds = setOf(currentCaptainId),
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
                Text(stringResource(Res.string.previous))
            }

            Button(
                onClick = {
                    onCaptainChanged(currentCaptainId)
                    onNext()
                },
                enabled = currentCaptainId != 0L,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}
