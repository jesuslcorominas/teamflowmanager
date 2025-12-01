package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary

/**
 * Wrapper composable that makes a player item a valid drop target.
 * Only active players (currently playing) can be drop targets.
 *
 * @param playerId The ID of this player
 * @param isPlaying Whether the player is currently active/playing
 * @param dragDropState The shared drag-drop state
 * @param onDrop Called when a valid drop occurs on this target
 * @param content The content to display (typically a PlayerItem)
 */
@Composable
fun DropTargetPlayerItem(
    playerId: Long,
    isPlaying: Boolean,
    dragDropState: DragDropState,
    onDrop: () -> Unit,
    content: @Composable () -> Unit,
) {
    var bounds by remember { mutableStateOf<Rect?>(null) }

    // Only active players can be drop targets
    val canBeDropTarget = isPlaying

    // Check if currently hovered based on state (not local state that can be lost)
    val isCurrentDropTarget = dragDropState.currentDropTargetId == playerId
    val showDropIndication = isCurrentDropTarget && dragDropState.isDragging && canBeDropTarget

    // Check if drag position is within bounds and update drop target
    LaunchedEffect(dragDropState.isDragging, dragDropState.dragPosition, bounds) {
        if (dragDropState.isDragging && canBeDropTarget && bounds != null) {
            val dragPos = dragDropState.dragPosition
            val inBounds = bounds!!.contains(dragPos)
            if (inBounds) {
                dragDropState.updateDropTarget(playerId, true)
            } else if (dragDropState.currentDropTargetId == playerId) {
                dragDropState.updateDropTarget(null, false)
            }
        }
    }

    // Handle drop when drag ends while this is the target
    // Watch dragJustEnded directly - when it becomes true and we're the current target, execute drop
    LaunchedEffect(dragDropState.dragJustEnded) {
        if (dragDropState.dragJustEnded && dragDropState.currentDropTargetId == playerId) {
            onDrop()
        }
    }

    // Pulsing animation for border
    val infiniteTransition = rememberInfiniteTransition(label = "dropTargetPulse")
    val animatedBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    // Scale animation for hover
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverScale"
    )

    // Only use animated values when showing drop indication
    val borderAlpha = if (showDropIndication) animatedBorderAlpha else 1f
    val scale = if (showDropIndication) animatedScale else 1f

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                bounds = coordinates.boundsInRoot()
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (showDropIndication) {
                    Modifier.border(
                        width = 3.dp,
                        color = Primary.copy(alpha = borderAlpha),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
