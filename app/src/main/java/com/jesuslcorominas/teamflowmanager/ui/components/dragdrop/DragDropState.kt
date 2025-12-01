package com.jesuslcorominas.teamflowmanager.ui.components.dragdrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.jesuslcorominas.teamflowmanager.domain.model.Player

/**
 * State holder for drag and drop operations.
 */
class DragDropState {
    var isDragging by mutableStateOf(false)
        private set

    var draggedPlayer by mutableStateOf<Player?>(null)
        private set

    var dragPosition by mutableStateOf(Offset.Zero)
        private set

    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    var currentDropTargetId by mutableStateOf<Long?>(null)
        private set

    var isValidDropTarget by mutableStateOf(false)
        private set

    // Flag to indicate drag just ended (for drop detection)
    var dragJustEnded by mutableStateOf(false)
        private set

    fun startDrag(player: Player, initialPosition: Offset) {
        draggedPlayer = player
        dragPosition = initialPosition
        dragOffset = Offset.Zero
        isDragging = true
        dragJustEnded = false
    }

    fun updateDragPosition(newPosition: Offset) {
        dragPosition = newPosition
    }

    fun updateDropTarget(playerId: Long?, isValid: Boolean) {
        currentDropTargetId = playerId
        isValidDropTarget = isValid
    }

    /**
     * Signal that dragging has ended but keep player info for drop handling.
     * Call [reset] after handling the drop.
     */
    fun endDrag() {
        isDragging = false
        dragJustEnded = true
    }

    /**
     * Fully reset the state after drop has been handled.
     */
    fun reset() {
        isDragging = false
        draggedPlayer = null
        dragPosition = Offset.Zero
        dragOffset = Offset.Zero
        currentDropTargetId = null
        isValidDropTarget = false
        dragJustEnded = false
    }
}

val LocalDragDropState = compositionLocalOf<DragDropState?> { null }

@Composable
fun rememberDragDropState(): DragDropState {
    return remember { DragDropState() }
}
