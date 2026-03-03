package com.jesuslcorominas.teamflowmanager.ui.matches.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.delete
import teamflowmanager.shared_ui.generated.resources.edit
import teamflowmanager.shared_ui.generated.resources.start_match

@Composable
fun PendingMatchCard(
    match: Match,
    hasMatchStarted: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStart: () -> Unit,
) {
    AppCard(
        modifier = Modifier
            .then(if (!hasMatchStarted) Modifier.clickable(onClick = onStart) else Modifier)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.opponent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                    Text(
                        text = match.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val date = match.dateTime?.let { DateFormatter.formatDate(it) } ?: ""
                    val time = match.dateTime?.let { DateFormatter.formatTimeOfDay(it) } ?: ""
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
                            contentDescription = stringResource(Res.string.edit),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasMatchStarted,
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.padding(start = TFMSpacing.spacing01))
                Text(text = stringResource(Res.string.start_match))
            }
        }
    }
}
