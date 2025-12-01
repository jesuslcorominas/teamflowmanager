package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Wrapper composable that makes a player item draggable via long-press gesture.
 * Only inactive players (not currently playing) can be dragged.
 *
 * @param player The player data
 * @param isPlaying Whether the player is currently active/playing
 * @param dragDropState The shared drag-drop state
 * @param onDragStart Called when drag starts
 * @param onDragEnd Called when drag ends
 * @param content The content to display (typically a PlayerItem)
 */
@Composable
fun DraggablePlayerItem(
    player: Player,
    isPlaying: Boolean,
    dragDropState: DragDropState,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var itemPosition by remember { mutableStateOf(Offset.Zero) }
    var isDraggingThis by remember { mutableStateOf(false) }

    // Animation for invalid drop (bounce back)
    val bounceOffsetX = remember { Animatable(0f) }
    val bounceOffsetY = remember { Animatable(0f) }
    val bounceScale = remember { Animatable(1f) }

    // Only inactive players can be dragged
    val canDrag = !isPlaying

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                itemPosition = coordinates.positionInRoot()
            }
            .offset {
                IntOffset(
                    bounceOffsetX.value.roundToInt(),
                    bounceOffsetY.value.roundToInt()
                )
            }
            .graphicsLayer {
                scaleX = bounceScale.value
                scaleY = bounceScale.value
                // Hide the original item when dragging
                alpha = if (isDraggingThis && dragDropState.isDragging) 0f else 1f
            }
            .then(
                if (canDrag) {
                    Modifier.pointerInput(player) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDraggingThis = true
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
                                if (!dragDropState.isValidDropTarget) {
                                    // Animate bounce back for invalid drop
                                    scope.launch {
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
                                isDraggingThis = false
                                onDragEnd()
                            },
                            onDragCancel = {
                                isDraggingThis = false
                                dragDropState.reset()
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
