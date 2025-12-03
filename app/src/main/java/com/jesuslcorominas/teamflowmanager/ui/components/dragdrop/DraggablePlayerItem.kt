package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import com.jesuslcorominas.teamflowmanager.domain.model.Player

/**
 * Wrapper composable that makes a player item draggable via long-press gesture.
 * Only inactive players (not currently playing) can be dragged.
 * 
 * Uses detectDragGesturesAfterLongPress to handle the complete drag lifecycle:
 * - Long press initiates the drag
 * - Drag movements update position
 * - Release ends the drag
 * 
 * NOTE: When this item scrolls off-screen (disposed by LazyColumn), the gesture
 * handler is cancelled but the drag continues to work because DragDropContainer
 * has a backup pointer tracker that continues tracking the drag at the container level.
 *
 * @param player The player data
 * @param isPlaying Whether the player is currently active/playing
 * @param dragDropState The shared drag-drop state
 * @param onDragStart Called when drag starts
 * @param content The content to display (typically a PlayerItem)
 */
@Composable
fun DraggablePlayerItem(
    player: Player,
    isPlaying: Boolean,
    dragDropState: DragDropState,
    onDragStart: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Track item position for initial drag position calculation
    var itemPosition = remember { Offset.Zero }

    // Animation for invalid drop (bounce back)
    val bounceScale = remember { Animatable(1f) }

    // Only inactive players can be dragged
    val canDrag = !isPlaying

    // Check if THIS player is being dragged (using state from DragDropState)
    val isThisBeingDragged = dragDropState.isDragging && dragDropState.draggedPlayerId == player.id
    
    // Check if drag just ended for THIS player (to show bounce animation for invalid drop)
    val dragJustEndedForThis = dragDropState.dragJustEnded && dragDropState.draggedPlayerId == player.id

    // Animate bounce when drag ends on invalid target
    LaunchedEffect(dragJustEndedForThis, dragDropState.isValidDropTarget) {
        if (dragJustEndedForThis && !dragDropState.isValidDropTarget) {
            // Animate bounce back for invalid drop
            bounceScale.animateTo(
                targetValue = 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
            bounceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                itemPosition = coordinates.positionInRoot()
            }
            .graphicsLayer {
                scaleX = bounceScale.value
                scaleY = bounceScale.value
                // Hide the original item when dragging
                alpha = if (isThisBeingDragged) 0f else 1f
            }
            .then(
                if (canDrag) {
                    Modifier.pointerInput(player.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                dragDropState.startDrag(
                                    player = player,
                                    initialPosition = itemPosition + offset
                                )
                                onDragStart()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newPosition = dragDropState.dragPosition + Offset(
                                    dragAmount.x,
                                    dragAmount.y
                                )
                                dragDropState.updateDragPosition(newPosition)
                            },
                            onDragEnd = {
                                dragDropState.endDrag()
                            },
                            onDragCancel = {
                                // Item scrolled off-screen and is being disposed.
                                // Signal that child gesture is no longer active so the 
                                // container can take over drag tracking.
                                dragDropState.onChildGestureCancelled()
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
