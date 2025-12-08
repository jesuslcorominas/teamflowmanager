package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.form.ListSummaryText
import com.jesuslcorominas.teamflowmanager.ui.players.components.JerseyBadge
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

private val GHOST_WIDTH = 300.dp
private val GHOST_HEIGHT = 80.dp

/**
 * Overlay that shows the ghost view of the player being dragged.
 * This should be placed at the root level of the composable hierarchy
 * to ensure it renders above all other content.
 *
 * @param dragDropState The shared drag-drop state
 * @param containerOffset The offset of the container in root coordinates
 */
@Composable
fun DragOverlay(
    dragDropState: DragDropState,
    containerOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier,
) {
    if (dragDropState.isDragging && dragDropState.draggedPlayer != null) {
        val player = dragDropState.draggedPlayer!!

        PlayerDragGhost(
            player = player,
            modifier = modifier
                .graphicsLayer {
                    // Position the ghost at the drag location, adjusted for container offset
                    // The drag position is in root coordinates, so subtract the container offset
                    translationX = dragDropState.dragPosition.x - containerOffset.x - (GHOST_WIDTH / 2).toPx()
                    translationY = dragDropState.dragPosition.y - containerOffset.y - (GHOST_HEIGHT / 2).toPx()
                    alpha = 0.9f
                    scaleX = 1.05f
                    scaleY = 1.05f
                    rotationZ = 2f // Slight rotation for dynamic feel
                }
        )
    }
}

/**
 * Ghost view showing player information during drag.
 * Displays jersey number, name, and positions.
 */
@Composable
private fun PlayerDragGhost(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .width(GHOST_WIDTH)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Primary,
                shape = RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(TFMSpacing.spacing03),
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JerseyBadge(
            number = player.number,
            size = 48
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "${player.firstName} ${player.lastName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (player.positions.isNotEmpty()) {
                ListSummaryText(
                    items = player.positions.map { it.toLocalizedString(context) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
