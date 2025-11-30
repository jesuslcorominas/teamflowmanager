package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import com.jesuslcorominas.teamflowmanager.ui.theme.AccentAffirmative

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
    var isHovered by remember { mutableStateOf(false) }

    // Only active players can be drop targets
    val canBeDropTarget = isPlaying

    val showDropIndication = isHovered && dragDropState.isDragging && canBeDropTarget

    // Check if drag position is within bounds
    LaunchedEffect(dragDropState.isDragging, dragDropState.dragPosition, bounds) {
        if (dragDropState.isDragging && canBeDropTarget && bounds != null) {
            val dragPos = dragDropState.dragPosition
            val inBounds = bounds!!.contains(dragPos)
            isHovered = inBounds
            if (inBounds) {
                dragDropState.updateDropTarget(playerId, true)
            } else if (dragDropState.currentDropTargetId == playerId) {
                dragDropState.updateDropTarget(null, false)
            }
        } else {
            isHovered = false
        }
    }

    // Handle drop when drag ends while hovering
    LaunchedEffect(dragDropState.isDragging) {
        if (!dragDropState.isDragging && isHovered) {
            onDrop()
            isHovered = false
        }
    }

    // Animations are always called but values are only used when needed
    // This follows Compose rules for composables
    val infiniteTransition = rememberInfiniteTransition(label = "dropTargetPulse")

    // Pulsing animation for border - only animated when showing drop indication
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = if (showDropIndication) 0.5f else 1f,
        targetValue = 1f,
        animationSpec = if (showDropIndication) {
            infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 0)
        },
        label = "borderAlpha"
    )

    // Scale animation for hover - animated based on showDropIndication
    val scale by animateFloatAsState(
        targetValue = if (showDropIndication) 1.03f else 1f,
        animationSpec = tween(durationMillis = if (showDropIndication) 150 else 100),
        label = "hoverScale"
    )

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
                        color = AccentAffirmative.copy(alpha = borderAlpha),
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
