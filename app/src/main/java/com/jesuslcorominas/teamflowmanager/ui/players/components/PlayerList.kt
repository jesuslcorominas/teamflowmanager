package com.jesuslcorominas.teamflowmanager.ui.players.components

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.theme.ShirtOrange
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.BebasNeueFontFamily
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMElevation
import com.jesuslcorominas.teamflowmanager.ui.theme.White
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerList(
    players: List<Player>,
    onEditClick: (Player) -> Unit,
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
            modifier = Modifier
                .fillMaxSize(),
            contentPadding =
                androidx.compose.foundation.layout
                    .PaddingValues(TFMSpacing.spacing04),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(players) { player ->
                PlayerItem(
                    player = player,
                    onEditClick = { onEditClick(player) },
                    onDeleteClick = { onDeleteClick(player) },
                )
            }
        }
    }
}

@Composable
private fun PlayerItem(
    player: Player,
    onEditClick: () -> Unit,
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundContrast)
                    .size(56.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(top = TFMSpacing.spacing02),
                        text = player.number.toString(),
                        fontFamily = BebasNeueFontFamily,
                        color = ContentContrast,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }

                Column {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TFMSpacing.spacing02)
                            .background(ShirtOrange)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TFMSpacing.spacing01)
                            .background(White)
                    )
                }
            }


            Row(modifier = Modifier.weight(1F)) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "${player.firstName} ${player.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = player.positions.joinToString(", ") { it.toLocalizedString(context) }
                            .ifEmpty { stringResource(R.string.no_positions) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_player_title),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlayerListPreview() {
    MaterialTheme {
        PlayerList(
            players =
                listOf(
                    Player(1, "John", "Doe", 3, listOf(Position.Forward)),
                    Player(2, "Jane", "Smith", 2, listOf(Position.Midfielder, Position.Defender)),
                    Player(3, "Bob", "Johnson", 17, listOf(Position.Goalkeeper)),
                ),
            onEditClick = {},
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
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}
