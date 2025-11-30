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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.delay

private const val AUTO_SCROLL_THRESHOLD = 100f
private const val AUTO_SCROLL_SPEED = 15f
private const val AUTO_SCROLL_DELAY = 16L

/**
 * Container that provides drag-drop functionality with auto-scroll support.
 * Wraps the content in a CompositionLocalProvider to share drag-drop state.
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
    var containerTop by remember { mutableStateOf(0f) }
    var containerBottom by remember { mutableStateOf(0f) }

    // Auto-scroll when dragging near edges
    LaunchedEffect(dragDropState.isDragging, dragDropState.dragPosition) {
        if (dragDropState.isDragging && containerBottom > containerTop) {
            val dragY = dragDropState.dragPosition.y

            while (dragDropState.isDragging) {
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

    CompositionLocalProvider(LocalDragDropState provides dragDropState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    containerTop = position.y
                    containerBottom = position.y + coordinates.size.height
                }
        ) {
            content()

            // Overlay for drag ghost - rendered on top of everything
            DragOverlay(dragDropState = dragDropState)
        }
    }
}
