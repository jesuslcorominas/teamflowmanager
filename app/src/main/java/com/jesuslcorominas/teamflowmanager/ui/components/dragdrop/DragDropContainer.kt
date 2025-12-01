package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.delay

private const val AUTO_SCROLL_THRESHOLD = 100f
private const val AUTO_SCROLL_SPEED = 15f
private const val AUTO_SCROLL_DELAY = 16L
private const val DROP_RESET_DELAY = 150L

/**
 * Container that provides drag-drop functionality with auto-scroll support.
 * Wraps the content in a CompositionLocalProvider to share drag-drop state.
 * 
 * This container also handles drag continuation when the original draggable item
 * scrolls off-screen, by tracking pointer events at the container level.
 *
 * @param dragDropState The shared drag-drop state
 * @param listState The LazyListState for auto-scroll functionality
 * @param content The content to display
 */
@Composable
fun DragDropContainer(
    dragDropState: DragDropState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var containerPosition by remember { mutableStateOf(Offset.Zero) }
    var containerTop by remember { mutableStateOf(0f) }
    var containerBottom by remember { mutableStateOf(0f) }
    
    // Track last known pointer position to calculate delta when taking over drag
    var lastPointerPosition by remember { mutableStateOf(Offset.Zero) }

    // Auto-scroll when dragging near edges
    LaunchedEffect(dragDropState.isDragging) {
        if (dragDropState.isDragging && containerBottom > containerTop) {
            while (dragDropState.isDragging) {
                // Read the current drag position inside the loop so it updates
                val dragY = dragDropState.dragPosition.y
                val distanceFromTop = dragY - containerTop
                val distanceFromBottom = containerBottom - dragY

                when {
                    distanceFromTop < AUTO_SCROLL_THRESHOLD && distanceFromTop > 0 -> {
                        // Scroll up
                        val scrollAmount = ((AUTO_SCROLL_THRESHOLD - distanceFromTop) / AUTO_SCROLL_THRESHOLD * AUTO_SCROLL_SPEED).toInt()
                        listState.dispatchRawDelta(-scrollAmount.toFloat())
                    }
                    distanceFromBottom < AUTO_SCROLL_THRESHOLD && distanceFromBottom > 0 -> {
                        // Scroll down
                        val scrollAmount = ((AUTO_SCROLL_THRESHOLD - distanceFromBottom) / AUTO_SCROLL_THRESHOLD * AUTO_SCROLL_SPEED).toInt()
                        listState.dispatchRawDelta(scrollAmount.toFloat())
                    }
                }
                delay(AUTO_SCROLL_DELAY)
            }
        }
    }

    // Reset state after drop ends if no valid target handled it
    LaunchedEffect(dragDropState.dragJustEnded) {
        if (dragDropState.dragJustEnded) {
            // Give drop targets time to handle the drop
            delay(DROP_RESET_DELAY)
            // If still in dragJustEnded state, no drop target handled it - reset
            if (dragDropState.dragJustEnded) {
                dragDropState.reset()
            }
        }
    }

    CompositionLocalProvider(LocalDragDropState provides dragDropState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    containerPosition = position
                    containerTop = position.y
                    containerBottom = position.y + coordinates.size.height
                }
                // This pointer input handles drag continuation when the original 
                // DraggablePlayerItem scrolls off-screen and is disposed.
                // It uses Initial pass to see events before children, but only
                // updates drag position when the child gesture is no longer active.
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Use Initial pass to see events before children
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            
                            // Always track pointer position
                            event.changes.firstOrNull()?.let { change ->
                                val currentPosition = change.position
                                
                                // Only handle drag if dragging AND child gesture is no longer active
                                // (child was disposed due to scrolling off-screen)
                                if (dragDropState.isDragging && !dragDropState.childGestureActive) {
                                    when (event.type) {
                                        PointerEventType.Move -> {
                                            // Calculate delta from last position
                                            val delta = currentPosition - lastPointerPosition
                                            if (delta != Offset.Zero) {
                                                val newPosition = dragDropState.dragPosition + delta
                                                dragDropState.updateDragPosition(newPosition)
                                            }
                                        }
                                        PointerEventType.Release -> {
                                            // Pointer released - end drag
                                            dragDropState.endDrag()
                                        }
                                    }
                                }
                                
                                // Always update last position for next frame
                                lastPointerPosition = currentPosition
                            }
                        }
                    }
                }
        ) {
            content()

            // Overlay for drag ghost - rendered on top of everything
            // Pass container position so the ghost can be positioned correctly
            DragOverlay(
                dragDropState = dragDropState,
                containerOffset = containerPosition
            )
        }
    }
}
