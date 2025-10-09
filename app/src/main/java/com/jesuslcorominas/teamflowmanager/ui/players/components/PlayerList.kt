package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

@Composable
fun PlayerList(players: List<Player>) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = stringResource(R.string.players_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(players) { player ->
                PlayerItem(player = player)
            }
        }
    }
}

@Composable
private fun PlayerItem(player: Player) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
        )
    }
}
