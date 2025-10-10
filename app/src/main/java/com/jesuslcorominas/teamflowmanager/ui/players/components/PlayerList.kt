package com.jesuslcorominas.teamflowmanager.ui.players.components

import TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMElevation
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

@Composable
fun PlayerList(
    players: List<Player>,
    onDeleteClick: (Player) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.players_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(TFMSpacing.spacing04),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(TFMSpacing.spacing04),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(players) { player ->
                PlayerItem(
                    player = player,
                    onDeleteClick = { onDeleteClick(player) },
                )
            }
        }
    }
}

@Composable
private fun PlayerItem(
    player: Player,
    onDeleteClick: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = TFMElevation.level1),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${if (player.number < 10) "0" else ""}${player.number}",
                style = MaterialTheme.typography.headlineLarge,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "${player.firstName} ${player.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = player.positions.joinToString(", ") { it.toLocalizedString(context) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_player_button),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerListPreview() {
    MaterialTheme {
        PlayerList(
            players =
                listOf(
                    Player(1, "John", "Doe", 3, listOf(Position.Forward)),
                    Player(2, "Jane", "Smith", 2, listOf(Position.Midfielder, Position.Defender)),
                    Player(3, "Bob", "Johnson", 7, listOf(Position.Goalkeeper)),
                ),
            onDeleteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerItemPreview() {
    MaterialTheme {
        PlayerItem(
            player =
                Player(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    number = 10,
                    positions = listOf(Position.Forward, Position.Midfielder),
                ),
            onDeleteClick = {},
        )
    }
}
