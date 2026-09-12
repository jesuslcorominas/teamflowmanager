package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.ic_substitution_arrows
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
 * Sized to match [com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerItem]: the same
 * [TFMSpacing.spacing04] padding around a default-size [JerseyBadge], so the height comes out equal
 * by construction rather than from a number copied across by eye. Sitting directly above the squad
 * list, a card that was visibly shorter and carried visibly smaller numbers read as a lesser kind
 * of row; it is the same kind of row, about two players instead of one.
 *
 * Deliberately not [com.jesuslcorominas.teamflowmanager.ui.components.card.SubstitutionCard]: that
 * one belongs to a finished match's timeline and is built around the minute it happened, which a
 * change that has not happened yet does not have.
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
                        start = TFMSpacing.spacing03,
                        end = TFMSpacing.spacing02,
                        top = TFMSpacing.spacing04,
                        bottom = TFMSpacing.spacing04,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JerseyBadge(number = item.playerIn.number)

            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            PlayerName(
                name = item.playerIn.firstName,
                color = SubstitutionGreen,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )

            SubstitutionArrows()

            PlayerName(
                name = item.playerOut.firstName,
                color = SubstitutionRed,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            JerseyBadge(number = item.playerOut.number)

            // The badge is the only thing telling two players with the same first name apart, so
            // the buttons keep their distance from it rather than crowding the number.
            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            // Side by side, with room between them. Stacked and touching, the play and the bin read
            // as one smudged control, and a mis-tap here deletes a change the coach meant to run.
            // Dropping the surname freed the width to lay them out flat; at 40dp each they stay
            // under the 56dp badge that sets this card's height, so it still matches a player's row.
            AppIconButton(
                modifier = Modifier.size(36.dp),
                internalModifier = Modifier.size(24.dp),
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(Res.string.pending_substitutions_execute_one),
                enabled = executeEnabled,
                onClick = onExecute,
            )

            Spacer(modifier = Modifier.width(TFMSpacing.spacing01))

            AppIconButton(
                modifier = Modifier.size(36.dp),
                internalModifier = Modifier.size(22.dp),
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(Res.string.pending_substitutions_remove_one),
                tint = MaterialTheme.colorScheme.error,
                onClick = onRemove,
            )
        }
    }
}

/**
 * The swap motif from the app icon: green arc over red arc, one coming on and one going off.
 *
 * The app's own mark rather than two Material arrows, so the card says "substitution" in the same
 * visual language as the launcher. Drawn with [Image], not `Icon`: it carries its own two colours
 * and a tint would flatten it to one.
 */
@Composable
private fun SubstitutionArrows() {
    Image(
        modifier = Modifier.size(28.dp),
        painter = painterResource(Res.drawable.ic_substitution_arrows),
        contentDescription = null,
    )
}

@Composable
private fun PlayerName(
    name: String,
    color: Color,
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
        // One line, and never two. Measured on a device, two 56dp badges, the arrows and the two
        // buttons leave this column narrower than some first names are wide, and a second line does
        // not rescue a single word — Compose breaks it mid-word instead, so "Alvaro" came out as
        // "Alvar/o" and "Martín" as "Martí/n", which is worse than any truncation. Capped at one
        // line a long name ends in an ellipsis, which at least reads as a name.
        //
        // bodySmall rather than bodyMedium for the same reason: the jersey number is what tells two
        // players apart now that surnames are gone, and it is the thing drawn large. The name only
        // has to confirm it.
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
