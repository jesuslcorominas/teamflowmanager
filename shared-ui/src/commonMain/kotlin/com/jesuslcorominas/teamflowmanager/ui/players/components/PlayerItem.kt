package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.components.form.ListSummaryText
import com.jesuslcorominas.teamflowmanager.ui.theme.GoalKeeperBadge
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import com.jesuslcorominas.teamflowmanager.ui.util.localizedName
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.captain_badge
import teamflowmanager.shared_ui.generated.resources.delete
import teamflowmanager.shared_ui.generated.resources.edit_player_title
import teamflowmanager.shared_ui.generated.resources.goalkeeper_badge

@Composable
fun PlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
    timeMillis: Long? = null,
    showCaptainBadge: Boolean = false,
    showGoalkeeperBadge: Boolean = false,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    showPositions: Boolean = true,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onMultiSelectionChange: ((Boolean) -> Unit)? = null,
    onSingleSelectionChange: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val effectiveModifier =
        when {
            onMultiSelectionChange != null && onSingleSelectionChange != null ->
                throw IllegalArgumentException(
                    "Only one of onMultiSelectionChange or onSingleSelectionChange can be provided.",
                )
            onMultiSelectionChange != null ->
                modifier.toggleable(
                    value = isSelected,
                    onValueChange = onMultiSelectionChange,
                    role = Role.Checkbox,
                )
            onSingleSelectionChange != null ->
                modifier.toggleable(
                    value = isSelected,
                    onValueChange = { onSingleSelectionChange() },
                    role = Role.RadioButton,
                )
            else -> modifier
        }

    val colors =
        onClick?.let {
            CardDefaults.cardColors(
                containerColor =
                    when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        isPlaying -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
            )
        }

    AppCard(
        modifier =
            effectiveModifier
                .fillMaxWidth()
                .then(onClick?.let { Modifier.clickable(onClick = onClick) } ?: Modifier),
        colors = colors,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JerseyBadge(number = player.number)

            Row(
                modifier = Modifier.weight(1F),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${player.firstName} ${player.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showPositions) {
                        ListSummaryText(
                            items = player.positions.map { it.localizedName() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (showCaptainBadge) {
                    PlayerBadge(badge = stringResource(Res.string.captain_badge))
                }

                if (showGoalkeeperBadge) {
                    PlayerBadge(
                        badge = stringResource(Res.string.goalkeeper_badge),
                        background = GoalKeeperBadge,
                    )
                }

                onEditClick?.let {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.edit_player_title),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                onDeleteClick?.let {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                onMultiSelectionChange?.let {
                    Checkbox(
                        modifier = Modifier.padding(TFMSpacing.spacing02),
                        checked = isSelected,
                        onCheckedChange = null,
                    )
                }

                onSingleSelectionChange?.let {
                    RadioButton(selected = isSelected, onClick = null)
                }

                if (timeMillis != null) {
                    Text(
                        text = formatTime(timeMillis),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerBadge(
    badge: String,
    background: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}
