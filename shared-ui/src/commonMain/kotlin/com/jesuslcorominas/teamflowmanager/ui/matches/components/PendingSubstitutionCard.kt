package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
 * One queued change: who comes on, who goes off, and the two things the coach can do with it.
 *
 * Laid out along a single line — incoming on the left, outgoing on the right, as the timeline's
 * card already does — rather than stacking the two players. Stacked, the jersey badges ended up
 * squeezed on top of each other and a two-digit number was barely readable, which is the one thing
 * on this card the coach reads at a glance.
 *
 * Deliberately not [com.jesuslcorominas.teamflowmanager.ui.components.card.SubstitutionCard]: that
 * one belongs to a finished match's timeline and is built around the minute it happened, which a
 * change that has not happened yet does not have. Several of these have to fit above the squad
 * list at once, so this one is a compact row rather than a tall card.
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
                        start = TFMSpacing.spacing02,
                        end = TFMSpacing.spacing01,
                        top = TFMSpacing.spacing02,
                        bottom = TFMSpacing.spacing02,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
        ) {
            JerseyBadge(number = item.playerIn.number, size = 30)

            PlayerName(
                name = "${item.playerIn.firstName} ${item.playerIn.lastName}",
                color = SubstitutionGreen,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )

            SubstitutionArrows()

            PlayerName(
                name = "${item.playerOut.firstName} ${item.playerOut.lastName}",
                color = SubstitutionRed,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )

            JerseyBadge(number = item.playerOut.number, size = 30)

            AppIconButton(
                modifier = Modifier.size(40.dp),
                internalModifier = Modifier.size(26.dp),
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
                modifier = Modifier.size(40.dp),
                internalModifier = Modifier.size(22.dp),
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.pending_substitutions_remove_one),
                tint = MaterialTheme.colorScheme.error,
                onClick = onRemove,
            )
        }
    }
}

/** Green in, red out — the direction each player is travelling, in the colours of the house. */
@Composable
private fun SubstitutionArrows() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = SubstitutionGreen,
        )
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = SubstitutionRed,
        )
    }
}

@Composable
private fun PlayerName(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = name,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = color,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
