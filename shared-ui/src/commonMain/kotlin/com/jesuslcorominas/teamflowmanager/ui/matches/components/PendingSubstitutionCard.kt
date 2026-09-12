package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.AppIconButton
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.players.components.JerseyBadge
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionGreen
import com.jesuslcorominas.teamflowmanager.ui.theme.SubstitutionRed
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.PendingSubstitutionItem
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_execute_one
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_remove_one

/**
 * One queued change: who goes off, who comes on, and the two things the coach can do with it.
 *
 * Deliberately not [com.jesuslcorominas.teamflowmanager.ui.components.card.SubstitutionCard]: that
 * one belongs to a finished match's timeline and is built around the minute it happened, which a
 * change that has not happened yet does not have. They share the house colours — red for the player
 * leaving, green for the one coming on — not a structure, and several of these have to fit on
 * screen at once above the squad list, so this one is a compact row instead of a tall card.
 */
@Composable
fun PendingSubstitutionCard(
    item: PendingSubstitutionItem,
    onExecute: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    executeEnabled: Boolean = true,
) {
    AppCard(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TFMSpacing.spacing03,
                        vertical = TFMSpacing.spacing02,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PendingSubstitutionPlayerRow(player = item.playerOut, isComingOn = false)
                PendingSubstitutionPlayerRow(player = item.playerIn, isComingOn = true)
            }

            AppIconButton(
                modifier = Modifier.size(44.dp),
                internalModifier = Modifier.size(28.dp),
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(Res.string.pending_substitutions_execute_one),
                enabled = executeEnabled,
                tint =
                    if (executeEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                onClick = onExecute,
            )

            AppIconButton(
                modifier = Modifier.size(44.dp),
                internalModifier = Modifier.size(24.dp),
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.pending_substitutions_remove_one),
                tint = MaterialTheme.colorScheme.error,
                onClick = onRemove,
            )
        }
    }
}

@Composable
private fun PendingSubstitutionPlayerRow(
    player: Player,
    isComingOn: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        JerseyBadge(number = player.number, size = 28)

        Text(
            text = "${player.firstName} ${player.lastName}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isComingOn) SubstitutionGreen else SubstitutionRed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
