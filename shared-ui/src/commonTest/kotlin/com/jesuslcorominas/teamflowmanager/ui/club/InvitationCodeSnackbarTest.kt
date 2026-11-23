package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationCodeSnackbarTest {
    @Test
    fun `copying the code shows the copied snackbar`() =
        runTest {
            val hostState = SnackbarHostState()

            backgroundScope.showReplacingSnackbar(hostState, COPIED)
            runCurrent()

            assertEquals(COPIED, hostState.currentSnackbarData?.visuals?.message)
        }

    @Test
    fun `copying twice shows the snackbar again instead of queueing behind the first one`() =
        runTest {
            val hostState = SnackbarHostState()

            backgroundScope.showReplacingSnackbar(hostState, COPIED)
            runCurrent()
            val first = hostState.currentSnackbarData

            backgroundScope.showReplacingSnackbar(hostState, COPIED)
            runCurrent()
            val second = hostState.currentSnackbarData

            assertEquals(COPIED, second?.visuals?.message)
            assertEquals(false, first === second)
        }

    @Test
    fun `dismissing the snackbar leaves the host empty`() =
        runTest {
            val hostState = SnackbarHostState()

            backgroundScope.showReplacingSnackbar(hostState, COPIED)
            runCurrent()
            hostState.currentSnackbarData?.dismiss()
            runCurrent()

            assertNull(hostState.currentSnackbarData)
        }

    private companion object {
        const val COPIED = "Invitation code copied"
    }
}
